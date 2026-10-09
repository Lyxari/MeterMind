package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;
import metermind.util.CurrencyUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Forecasts next month's bill based on the same month from last year.
 * Formula: same-month-last-year consumption × latest effective rate
 * Falls back to -1 if no data exists for that month last year.
 */
public class SeasonalForecaster implements Forecaster {

    @Override
    public double forecast(BillHistory history, UtilityType type) {
        List<UtilityBill> bills = history.getByUtilityType(type);
        if (bills.isEmpty()) return -1;

        // Determine the "next" month to forecast
        UtilityBill latest = bills.get(bills.size() - 1);
        int nextMonth = latest.getMonth() % 12 + 1;
        int nextYear = latest.getMonth() == 12 ? latest.getYear() + 1 : latest.getYear();

        // Look for the same month last year
        int lookupYear = nextYear - 1;
        Optional<UtilityBill> sameMonthLastYear = history.getByYearMonth(lookupYear, nextMonth, type);

        if (!sameMonthLastYear.isPresent()) {
            return -1; // No historical data for this month
        }

        double historicalConsumption = sameMonthLastYear.get().getConsumption();
        double latestRate = latest.getEffectiveRate();

        return CurrencyUtil.round2(historicalConsumption * latestRate);
    }

    @Override
    public String getMethodName() {
        return "Seasonal (Same Month Last Year)";
    }
}
