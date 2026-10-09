package metermind.alerts;

import metermind.model.UtilityType;
import metermind.util.CurrencyUtil;

/**
 * Alert triggered when monthly spending exceeds the configured budget limit.
 */
public class BudgetAlert extends Alert {

    private final UtilityType type; // null = overall budget
    private final double spent;
    private final double limit;

    /**
     * @param type  The utility type, or null for overall budget
     * @param spent Amount spent this month
     * @param limit The budget limit
     */
    public BudgetAlert(UtilityType type, double spent, double limit) {
        super();
        this.type = type;
        this.spent = spent;
        this.limit = limit;
    }

    @Override
    public AlertSeverity getSeverity() {
        return AlertSeverity.DANGER;
    }

    @Override
    public String getTitle() {
        return "Budget Exceeded";
    }

    @Override
    public String getMessage() {
        String category = (type != null) ? type.getDisplayName() : "Overall";
        double over = spent - limit;
        return String.format("%s spending of %s exceeds the budget limit of %s by %s.",
                category,
                CurrencyUtil.formatPeso(spent),
                CurrencyUtil.formatPeso(limit),
                CurrencyUtil.formatPeso(over));
    }

    public UtilityType getType() { return type; }
    public double getSpent() { return spent; }
    public double getLimit() { return limit; }
}
