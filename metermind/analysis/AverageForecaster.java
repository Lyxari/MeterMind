package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;
import metermind.util.CurrencyUtil;

import java.util.List;

/**
 * Forecasts next month's bill using a rolling 3-month average.
 * Formula: avg(last 3 months' consumption) × latest effective rate
 */
public class AverageForecaster implements Forecaster {

    private static final int LOOKBACK_MONTHS = 3;

    @Override
    public double forecast(BillHistory history, UtilityType type) {
        List<UtilityBill> bills = history.getByUtilityType(type);

        if (bills.size() < LOOKBACK_MONTHS) {
            return -1; // Insufficient data
        }

        // Calculate average consumption from last 3 months
        double avgConsumption = history.getAverageConsumption(type, LOOKBACK_MONTHS);

        // Use the most recent effective rate
        UtilityBill latest = bills.get(bills.size() - 1);
        double latestRate = latest.getEffectiveRate();

        return CurrencyUtil.round2(avgConsumption * latestRate);
    }

    @Override
    public String getMethodName() {
        return "3-Month Rolling Average";
    }
}
