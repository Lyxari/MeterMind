package metermind.model;

/**
 * Represents monthly budget limits for utility spending.
 * A limit of 0 means no limit is set (disabled).
 */
public class Budget {

    private double overallMonthlyLimit;
    private double electricityLimit;
    private double waterLimit;

    public Budget() {
        this.overallMonthlyLimit = 0;
        this.electricityLimit = 0;
        this.waterLimit = 0;
    }

    public Budget(double overallMonthlyLimit, double electricityLimit, double waterLimit) {
        this.overallMonthlyLimit = overallMonthlyLimit;
        this.electricityLimit = electricityLimit;
        this.waterLimit = waterLimit;
    }

    /**
     * Checks if total spending exceeds the overall monthly limit.
     * Returns false if the limit is disabled (0).
     */
    public boolean isOverallExceeded(double totalSpent) {
        return overallMonthlyLimit > 0 && totalSpent > overallMonthlyLimit;
    }

    /**
     * Checks if spending for a specific utility type exceeds its limit.
     * Returns false if the limit is disabled (0).
     */
    public boolean isExceeded(UtilityType type, double spent) {
        double limit = getLimitForType(type);
        return limit > 0 && spent > limit;
    }

    /**
     * Returns the budget limit for a specific utility type.
     */
    public double getLimitForType(UtilityType type) {
        switch (type) {
            case ELECTRICITY: return electricityLimit;
            case WATER: return waterLimit;
            default: return 0;
        }
    }

    /**
     * Calculates the percentage of budget used. Returns 0 if limit is disabled.
     */
    public double getUsagePercent(double spent, double limit) {
        if (limit <= 0) return 0;
        return (spent / limit) * 100.0;
    }

    public boolean hasAnyLimit() {
        return overallMonthlyLimit > 0 || electricityLimit > 0 || waterLimit > 0;
    }

    // --- Getters and Setters ---

    public double getOverallMonthlyLimit() { return overallMonthlyLimit; }
    public void setOverallMonthlyLimit(double limit) { this.overallMonthlyLimit = limit; }

    public double getElectricityLimit() { return electricityLimit; }
    public void setElectricityLimit(double limit) { this.electricityLimit = limit; }

    public double getWaterLimit() { return waterLimit; }
    public void setWaterLimit(double limit) { this.waterLimit = limit; }

    @Override
    public String toString() {
        return String.format("Budget[overall=₱%.2f, electricity=₱%.2f, water=₱%.2f]",
                overallMonthlyLimit, electricityLimit, waterLimit);
    }
}
