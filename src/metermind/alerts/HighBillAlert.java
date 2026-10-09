package metermind.alerts;

import metermind.model.UtilityBill;
import metermind.util.CurrencyUtil;

/**
 * Alert triggered when a bill exceeds the recent average by a configurable threshold.
 * Default: triggers if current bill > avg(last 3 months) × (1 + 20%).
 */
public class HighBillAlert extends Alert {

    private final UtilityBill bill;
    private final double average;
    private final double thresholdPercent;

    public HighBillAlert(UtilityBill bill, double average, double thresholdPercent) {
        super();
        this.bill = bill;
        this.average = average;
        this.thresholdPercent = thresholdPercent;
    }

    @Override
    public AlertSeverity getSeverity() {
        return AlertSeverity.WARNING;
    }

    @Override
    public String getTitle() {
        return "High Bill Detected";
    }

    @Override
    public String getMessage() {
        double percentOver = ((bill.getAmountDue() - average) / average) * 100.0;
        return String.format("Your %s bill of %s is %.1f%% higher than the 3-month average of %s.",
                bill.getUtilityType().getDisplayName(),
                CurrencyUtil.formatPeso(bill.getAmountDue()),
                percentOver,
                CurrencyUtil.formatPeso(average));
    }

    public UtilityBill getBill() { return bill; }
    public double getAverage() { return average; }
}
