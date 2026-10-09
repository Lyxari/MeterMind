package metermind.analysis;

import metermind.model.UtilityBill;
import metermind.util.CurrencyUtil;

/**
 * Data carrier for the result of a bill-to-bill gap analysis.
 * Decomposes the total cost change into Usage Effect and Rate Effect.
 *
 * Formulas:
 *   Effective Rate = Amount Due / Consumption
 *   Usage Effect   = (Consumption₂ - Consumption₁) × Rate₁
 *   Rate Effect    = (Rate₂ - Rate₁) × Consumption₂
 *   Total Change   = Usage Effect + Rate Effect ≡ Amount₂ - Amount₁
 */
public class GapResult {

    private final UtilityBill bill1; // Earlier bill
    private final UtilityBill bill2; // Later bill
    private final double amountChange;
    private final double percentChange;
    private final double usageEffect;
    private final double rateEffect;
    private final double rate1;
    private final double rate2;
    private final double consumptionDelta;
    private final double rateDelta;

    public GapResult(UtilityBill bill1, UtilityBill bill2) {
        this.bill1 = bill1;
        this.bill2 = bill2;

        this.rate1 = bill1.getEffectiveRate();
        this.rate2 = bill2.getEffectiveRate();

        this.consumptionDelta = bill2.getConsumption() - bill1.getConsumption();
        this.rateDelta = rate2 - rate1;

        this.usageEffect = CurrencyUtil.round2(consumptionDelta * rate1);
        this.rateEffect = CurrencyUtil.round2(rateDelta * bill2.getConsumption());
        this.amountChange = CurrencyUtil.round2(bill2.getAmountDue() - bill1.getAmountDue());

        if (bill1.getAmountDue() != 0) {
            this.percentChange = CurrencyUtil.round2((amountChange / bill1.getAmountDue()) * 100.0);
        } else {
            this.percentChange = 0;
        }
    }

    /**
     * Returns a human-readable explanation of the gap analysis.
     */
    public String explain() {
        StringBuilder sb = new StringBuilder();
        String type = bill2.getUtilityType().getDisplayName().toLowerCase();
        String unit = bill2.getUnit();

        String direction = amountChange >= 0 ? "increased" : "decreased";
        sb.append(String.format("Your %s bill %s by %s (%s).\n",
                type, direction,
                CurrencyUtil.formatAmountChange(Math.abs(amountChange)),
                CurrencyUtil.formatPercentChange(percentChange)));

        // Usage effect explanation
        if (consumptionDelta != 0) {
            String usageDir = consumptionDelta > 0 ? "higher" : "lower";
            sb.append(String.format("  • %s came from %s usage (%s%.2f %s).\n",
                    CurrencyUtil.formatPeso(Math.abs(usageEffect)),
                    usageDir,
                    consumptionDelta > 0 ? "+" : "", consumptionDelta, unit));
        } else {
            sb.append("  • Consumption was unchanged.\n");
        }

        // Rate effect explanation
        if (rateDelta != 0) {
            String rateDir = rateDelta > 0 ? "increase" : "decrease";
            sb.append(String.format("  • %s came from a rate %s (%s%.4f/%s).\n",
                    CurrencyUtil.formatPeso(Math.abs(rateEffect)),
                    rateDir,
                    rateDelta > 0 ? "+" : "", rateDelta, unit));
        } else {
            sb.append("  • The effective rate was unchanged.\n");
        }

        return sb.toString();
    }

    /**
     * Returns a short one-line summary of the change.
     */
    public String getSummary() {
        return String.format("%s (%s)",
                CurrencyUtil.formatAmountChange(amountChange),
                CurrencyUtil.formatPercentChange(percentChange));
    }

    /**
     * Returns true if the bill increased.
     */
    public boolean isIncrease() {
        return amountChange > 0;
    }

    // --- Getters ---

    public UtilityBill getBill1() { return bill1; }
    public UtilityBill getBill2() { return bill2; }
    public double getAmountChange() { return amountChange; }
    public double getPercentChange() { return percentChange; }
    public double getUsageEffect() { return usageEffect; }
    public double getRateEffect() { return rateEffect; }
    public double getRate1() { return rate1; }
    public double getRate2() { return rate2; }
    public double getConsumptionDelta() { return consumptionDelta; }
    public double getRateDelta() { return rateDelta; }
}
