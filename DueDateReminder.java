package metermind.alerts;

import metermind.model.UtilityBill;
import metermind.util.CurrencyUtil;
import metermind.util.DateUtil;

/**
 * Alert for unpaid bills that are due soon or overdue.
 * Severity escalates based on urgency:
 *   - Due in N days → INFO
 *   - Due tomorrow/today → WARNING
 *   - Overdue → DANGER
 */
public class DueDateReminder extends Alert {

    private final UtilityBill bill;
    private final long daysUntilDue;

    public DueDateReminder(UtilityBill bill) {
        super();
        this.bill = bill;
        this.daysUntilDue = DateUtil.daysUntilDue(bill.getDueDate());
    }

    @Override
    public AlertSeverity getSeverity() {
        if (daysUntilDue < 0) return AlertSeverity.DANGER;
        if (daysUntilDue <= 1) return AlertSeverity.WARNING;
        return AlertSeverity.INFO;
    }

    @Override
    public String getTitle() {
        if (daysUntilDue < 0) return "Overdue Bill";
        return "Payment Reminder";
    }

    @Override
    public String getMessage() {
        return String.format("%s bill of %s — %s (%s).",
                bill.getUtilityType().getDisplayName(),
                CurrencyUtil.formatPeso(bill.getAmountDue()),
                DateUtil.getDueStatus(bill.getDueDate()),
                DateUtil.formatDisplay(bill.getDueDate()));
    }

    public UtilityBill getBill() { return bill; }
    public long getDaysUntilDue() { return daysUntilDue; }
}
