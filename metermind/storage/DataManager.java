package metermind.storage;

import metermind.model.*;
import metermind.util.DateUtil;

import java.io.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

/**
 * Manages persistent storage for bills (CSV) and application settings (properties file).
 * Resilient to corrupted rows — skips bad lines and logs warnings.
 */
public class DataManager {

    private static final String CSV_HEADER = "id,type,provider,year,month,consumption,amountDue,dueDate,isPaid";
    private static final String DEFAULT_BILLS_FILE = "bills.csv";
    private static final String DEFAULT_SETTINGS_FILE = "settings.properties";

    private final File billsFile;
    private final File settingsFile;

    public DataManager() {
        this(DEFAULT_BILLS_FILE, DEFAULT_SETTINGS_FILE);
    }

    public DataManager(String billsPath, String settingsPath) {
        this.billsFile = new File(billsPath);
        this.settingsFile = new File(settingsPath);
    }

    // ========================
    //   Bill CSV Operations
    // ========================

    /**
     * Loads all bills from the CSV file into a BillHistory.
     * Skips corrupted rows gracefully and prints warnings to stderr.
     *
     * @throws DataFileException if the file exists but cannot be read
     */
    public BillHistory loadBills() throws DataFileException {
        BillHistory history = new BillHistory();

        if (!billsFile.exists()) {
            return history;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(billsFile))) {
            String line = reader.readLine(); // Skip header
            if (line == null) return history;

            // Validate header
            if (!line.trim().equals(CSV_HEADER)) {
                System.err.println("[MeterMind] Warning: CSV header mismatch. Expected: " + CSV_HEADER);
            }

            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    UtilityBill bill = parseCsvLine(line);
                    history.addBill(bill);
                } catch (Exception e) {
                    System.err.println("[MeterMind] Warning: Skipping corrupted row at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataFileException("Failed to read bills file: " + billsFile.getPath(), e);
        }

        return history;
    }

    /**
     * Saves all bills from a BillHistory to the CSV file.
     * Writes to a temporary file first, then renames for atomicity.
     *
     * @throws DataFileException if the file cannot be written
     */
    public void saveBills(BillHistory history) throws DataFileException {
        File tempFile = new File(billsFile.getPath() + ".tmp");

        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            writer.println(CSV_HEADER);

            List<UtilityBill> bills = history.getAllSorted();
            for (UtilityBill bill : bills) {
                writer.println(toCsvLine(bill));
            }
        } catch (IOException e) {
            throw new DataFileException("Failed to write bills file: " + tempFile.getPath(), e);
        }

        // Atomic rename
        if (billsFile.exists() && !billsFile.delete()) {
            throw new DataFileException("Failed to replace existing bills file.");
        }
        if (!tempFile.renameTo(billsFile)) {
            throw new DataFileException("Failed to finalize bills file save.");
        }
    }

    /**
     * Parses a single CSV line into a UtilityBill object.
     */
    private UtilityBill parseCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length != 9) {
            throw new IllegalArgumentException("Expected 9 fields, got " + parts.length);
        }

        String id = parts[0].trim();
        String typeStr = parts[1].trim().toUpperCase();
        String provider = parts[2].trim();
        int year = Integer.parseInt(parts[3].trim());
        int month = Integer.parseInt(parts[4].trim());
        double consumption = Double.parseDouble(parts[5].trim());
        double amountDue = Double.parseDouble(parts[6].trim());
        LocalDate dueDate = DateUtil.parse(parts[7].trim());
        boolean isPaid = Boolean.parseBoolean(parts[8].trim());

        if (dueDate == null) {
            throw new IllegalArgumentException("Invalid date format: " + parts[7].trim());
        }

        UtilityType type = UtilityType.valueOf(typeStr);

        switch (type) {
            case ELECTRICITY:
                return new ElectricityBill(id, provider, year, month, consumption, amountDue, dueDate, isPaid);
            case WATER:
                return new WaterBill(id, provider, year, month, consumption, amountDue, dueDate, isPaid);
            default:
                throw new IllegalArgumentException("Unknown utility type: " + typeStr);
        }
    }

    /**
     * Converts a UtilityBill to a CSV line string.
     */
    private String toCsvLine(UtilityBill bill) {
        return String.join(",",
                bill.getId(),
                bill.getUtilityType().name(),
                bill.getProvider(),
                String.valueOf(bill.getYear()),
                String.valueOf(bill.getMonth()),
                String.valueOf(bill.getConsumption()),
                String.valueOf(bill.getAmountDue()),
                DateUtil.format(bill.getDueDate()),
                String.valueOf(bill.isPaid())
        );
    }

    // ========================
    //   Settings Operations
    // ========================

    /**
     * Loads budget and alert settings from the properties file.
     * Returns default values if the file doesn't exist.
     */
    public Budget loadBudget() {
        Properties props = loadProperties();
        Budget budget = new Budget();

        budget.setOverallMonthlyLimit(getDoubleProp(props, "budget.overall", 0));
        budget.setElectricityLimit(getDoubleProp(props, "budget.electricity", 0));
        budget.setWaterLimit(getDoubleProp(props, "budget.water", 0));

        return budget;
    }

    /**
     * Saves budget settings to the properties file.
     */
    public void saveBudget(Budget budget) throws DataFileException {
        Properties props = loadProperties();

        props.setProperty("budget.overall", String.valueOf(budget.getOverallMonthlyLimit()));
        props.setProperty("budget.electricity", String.valueOf(budget.getElectricityLimit()));
        props.setProperty("budget.water", String.valueOf(budget.getWaterLimit()));

        saveProperties(props);
    }

    /**
     * Loads the high-bill threshold percentage (default: 20%).
     */
    public double loadHighBillThreshold() {
        Properties props = loadProperties();
        return getDoubleProp(props, "alert.highbill.threshold", 20.0);
    }

    /**
     * Saves the high-bill threshold percentage.
     */
    public void saveHighBillThreshold(double threshold) throws DataFileException {
        Properties props = loadProperties();
        props.setProperty("alert.highbill.threshold", String.valueOf(threshold));
        saveProperties(props);
    }

    /**
     * Loads the reminder days threshold (default: 3 days before due).
     */
    public int loadReminderDays() {
        Properties props = loadProperties();
        return getIntProp(props, "alert.reminder.days", 3);
    }

    /**
     * Saves the reminder days threshold.
     */
    public void saveReminderDays(int days) throws DataFileException {
        Properties props = loadProperties();
        props.setProperty("alert.reminder.days", String.valueOf(days));
        saveProperties(props);
    }

    // ========================
    //   Properties Helpers
    // ========================

    private Properties loadProperties() {
        Properties props = new Properties();
        if (settingsFile.exists()) {
            try (FileReader reader = new FileReader(settingsFile)) {
                props.load(reader);
            } catch (IOException e) {
                System.err.println("[MeterMind] Warning: Could not load settings: " + e.getMessage());
            }
        }
        return props;
    }

    private void saveProperties(Properties props) throws DataFileException {
        try (FileWriter writer = new FileWriter(settingsFile)) {
            props.store(writer, "MeterMind Settings");
        } catch (IOException e) {
            throw new DataFileException("Failed to save settings: " + e.getMessage(), e);
        }
    }

    private double getDoubleProp(Properties props, String key, double defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private int getIntProp(Properties props, String key, int defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    // ========================
    //   File Path Accessors
    // ========================

    public File getBillsFile() { return billsFile; }
    public File getSettingsFile() { return settingsFile; }

    /**
     * Checks if a bills CSV file already exists.
     */
    public boolean billsFileExists() {
        return billsFile.exists();
    }
}
