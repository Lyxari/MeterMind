package metermind.model;

import java.time.LocalDate;

/**
 * Concrete bill class for water consumption, measured in cubic meters (m³).
 */
public class WaterBill extends UtilityBill {

    public WaterBill(String provider, int year, int month,
                     double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        super(UtilityType.WATER, provider, year, month, consumption, amountDue, dueDate, isPaid);
    }

    public WaterBill(String id, String provider, int year, int month,
                     double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        super(id, UtilityType.WATER, provider, year, month, consumption, amountDue, dueDate, isPaid);
    }

    @Override
    public String getUnit() {
        return "m³";
    }
}
