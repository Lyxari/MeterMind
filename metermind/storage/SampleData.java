package metermind.storage;

import metermind.model.*;

import java.time.LocalDate;

/**
 * Generates 24 months of realistic sample bill data for MeterMind.
 * Covers January 2025 through December 2026 with seasonal variations.
 * Used on first launch when no bills.csv exists.
 */
public class SampleData {

    // Electricity: seasonal consumption pattern (kWh) — peaks in hot months (Apr-Jun)
    private static final double[] ELEC_CONSUMPTION = {
        // 2025: Jan-Dec
        180, 175, 210, 280, 320, 310, 250, 230, 200, 195, 185, 190,
        // 2026: Jan-Dec
        185, 180, 220, 290, 335, 325, 260, 245, 210, 200, 190, 195
    };

    // Electricity effective rate per kWh (gradual increase over 2 years)
    private static final double[] ELEC_RATE = {
        11.50, 11.50, 11.80, 12.00, 12.20, 12.20, 12.00, 11.80, 11.60, 11.50, 11.50, 11.50,
        11.80, 11.80, 12.10, 12.30, 12.50, 12.50, 12.30, 12.10, 11.90, 11.80, 11.80, 11.80
    };

    // Water: consumption pattern (m³) — fairly stable with slight summer increase
    private static final double[] WATER_CONSUMPTION = {
        // 2025: Jan-Dec
        14, 13, 15, 18, 22, 24, 20, 18, 16, 15, 14, 14,
        // 2026: Jan-Dec
        15, 14, 16, 19, 23, 25, 21, 19, 17, 16, 15, 15
    };

    // Water effective rate per m³
    private static final double[] WATER_RATE = {
        30.50, 30.50, 30.50, 31.00, 31.00, 31.00, 31.50, 31.50, 31.50, 32.00, 32.00, 32.00,
        32.50, 32.50, 32.50, 33.00, 33.00, 33.00, 33.50, 33.50, 33.50, 34.00, 34.00, 34.00
    };

    private SampleData() {
        // Utility class — no instantiation
    }

    /**
     * Generates a BillHistory with 24 months of sample data (48 bills total).
     * Electricity bills use "Meralco" as provider.
     * Water bills use "Maynilad" as provider.
     */
    public static BillHistory generate() {
        BillHistory history = new BillHistory();

        int index = 0;
        for (int year = 2025; year <= 2026; year++) {
            for (int month = 1; month <= 12; month++) {
                try {
                    // Electricity bill
                    double elecConsumption = ELEC_CONSUMPTION[index];
                    double elecAmount = Math.round(elecConsumption * ELEC_RATE[index] * 100.0) / 100.0;
                    LocalDate elecDueDate = LocalDate.of(year, month, 1).plusMonths(1).plusDays(4);
                    boolean elecPaid = isInPast(year, month);

                    ElectricityBill elecBill = new ElectricityBill(
                            "Meralco", year, month, elecConsumption, elecAmount, elecDueDate, elecPaid);
                    history.addBill(elecBill);

                    // Water bill
                    double waterConsumption = WATER_CONSUMPTION[index];
                    double waterAmount = Math.round(waterConsumption * WATER_RATE[index] * 100.0) / 100.0;
                    LocalDate waterDueDate = LocalDate.of(year, month, 1).plusMonths(1).plusDays(9);
                    boolean waterPaid = isInPast(year, month);

                    WaterBill waterBill = new WaterBill(
                            "Maynilad", year, month, waterConsumption, waterAmount, waterDueDate, waterPaid);
                    history.addBill(waterBill);

                } catch (DuplicateBillException e) {
                    // Should not happen with generated data
                    System.err.println("[MeterMind] Sample data error: " + e.getMessage());
                }

                index++;
            }
        }

        return history;
    }

    /**
     * Determines if a bill in the past should be marked as paid.
     * Bills before the current month are marked as paid.
     */
    private static boolean isInPast(int year, int month) {
        LocalDate now = LocalDate.now();
        LocalDate billMonth = LocalDate.of(year, month, 1);
        return billMonth.isBefore(now.withDayOfMonth(1));
    }
}
