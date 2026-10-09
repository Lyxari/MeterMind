package metermind.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all utility bills.
 * Encapsulates common billing data and provides derived calculations.
 */
public abstract class UtilityBill implements Comparable<UtilityBill> {

    private final String id;
    private UtilityType utilityType;
    private String provider;
    private int year;
    private int month;
    private double consumption;
    private double amountDue;
    private LocalDate dueDate;
    private boolean isPaid;

    /**
     * Constructs a new UtilityBill with an auto-generated UUID.
     */
    protected UtilityBill(UtilityType utilityType, String provider, int year, int month,
                          double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        this(UUID.randomUUID().toString(), utilityType, provider, year, month,
             consumption, amountDue, dueDate, isPaid);
    }

    /**
     * Constructs a UtilityBill with a specific ID (used when loading from storage).
     */
    protected UtilityBill(String id, UtilityType utilityType, String provider, int year, int month,
                          double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        this.id = id;
        this.utilityType = utilityType;
        this.provider = provider;
        this.year = year;
        this.month = month;
        this.consumption = consumption;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
    }

    /**
     * Returns the measurement unit for this bill type (e.g., "kWh", "m³").
     */
    public abstract String getUnit();

    /**
     * Calculates the effective rate (cost per unit of consumption).
     * Returns 0 if consumption is zero to avoid division by zero.
     */
    public double getEffectiveRate() {
        if (consumption == 0) return 0;
        return amountDue / consumption;
    }

    // --- Getters ---

    public String getId() { return id; }
    public UtilityType getUtilityType() { return utilityType; }
    public String getProvider() { return provider; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public double getConsumption() { return consumption; }
    public double getAmountDue() { return amountDue; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isPaid() { return isPaid; }

    // --- Setters ---

    public void setProvider(String provider) { this.provider = provider; }
    public void setYear(int year) { this.year = year; }
    public void setMonth(int month) { this.month = month; }
    public void setConsumption(double consumption) { this.consumption = consumption; }
    public void setAmountDue(double amountDue) { this.amountDue = amountDue; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPaid(boolean paid) { this.isPaid = paid; }

    /**
     * Bills are sorted chronologically: by year, then month, then utility type.
     */
    @Override
    public int compareTo(UtilityBill other) {
        int cmp = Integer.compare(this.year, other.year);
        if (cmp != 0) return cmp;
        cmp = Integer.compare(this.month, other.month);
        if (cmp != 0) return cmp;
        return this.utilityType.compareTo(other.utilityType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtilityBill that = (UtilityBill) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("%s %s %d-%02d: %.2f %s, ₱%.2f (Due: %s, %s)",
                utilityType.getDisplayName(), provider, year, month,
                consumption, getUnit(), amountDue, dueDate,
                isPaid ? "Paid" : "Unpaid");
    }
}
