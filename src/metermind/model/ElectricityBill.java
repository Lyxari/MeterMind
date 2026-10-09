package metermind.model;

import java.time.LocalDate;

/**
 * Concrete bill class for electricity consumption, measured in kWh.
 */
public class ElectricityBill extends UtilityBill {

    public ElectricityBill(String provider, int year, int month,
                           double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        super(UtilityType.ELECTRICITY, provider, year, month, consumption, amountDue, dueDate, isPaid);
    }

    public ElectricityBill(String id, String provider, int year, int month,
                           double consumption, double amountDue, LocalDate dueDate, boolean isPaid) {
        super(id, UtilityType.ELECTRICITY, provider, year, month, consumption, amountDue, dueDate, isPaid);
    }

    @Override
    public String getUnit() {
        return "kWh";
    }
}
