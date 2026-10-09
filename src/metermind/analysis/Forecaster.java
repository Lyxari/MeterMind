package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityType;

/**
 * Interface for bill forecasting strategies.
 * Implementations predict the next month's bill amount based on historical data.
 */
public interface Forecaster {

    /**
     * Forecasts the next bill amount for a given utility type.
     *
     * @param history The bill history to base the forecast on
     * @param type    The utility type to forecast
     * @return Predicted amount in pesos, or -1 if insufficient data
     */
    double forecast(BillHistory history, UtilityType type);

    /**
     * Returns a human-readable description of this forecasting strategy.
     */
    String getMethodName();
}
