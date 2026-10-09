package metermind.util;

import java.text.DecimalFormat;

/**
 * Utility class for currency formatting and rounding.
 * Uses Philippine Peso (₱) as the default currency symbol.
 */
public final class CurrencyUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat RATE_FORMAT = new DecimalFormat("#,##0.0000");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("+#,##0.0;-#,##0.0");

    private CurrencyUtil() {
        // Utility class — no instantiation
    }

    /**
     * Rounds a value to 2 decimal places.
     * Used to maintain display-clean currency values without BigDecimal overhead.
     */
    public static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Rounds a value to 4 decimal places (for effective rates).
     */
    public static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    /**
     * Formats a value as Philippine Peso currency string.
     * Example: 2800.5 → "₱2,800.50"
     */
    public static String formatPeso(double amount) {
        return "₱" + CURRENCY_FORMAT.format(amount);
    }

    /**
     * Formats a rate value with 4 decimal places.
     * Example: 14.0 → "₱14.0000/unit"
     */
    public static String formatRate(double rate, String unit) {
        return "₱" + RATE_FORMAT.format(rate) + "/" + unit;
    }

    /**
     * Formats a percentage change with sign.
     * Example: 33.93 → "+33.9%", -12.5 → "-12.5%"
     */
    public static String formatPercentChange(double percent) {
        return PERCENT_FORMAT.format(percent) + "%";
    }

    /**
     * Formats a currency change with sign.
     * Example: 950.0 → "+₱950.00", -200.0 → "-₱200.00"
     */
    public static String formatAmountChange(double amount) {
        if (amount >= 0) {
            return "+₱" + CURRENCY_FORMAT.format(amount);
        } else {
            return "-₱" + CURRENCY_FORMAT.format(Math.abs(amount));
        }
    }

    /**
     * Formats a consumption value with its unit.
     * Example: 250.0, "kWh" → "250.00 kWh"
     */
    public static String formatConsumption(double consumption, String unit) {
        return CURRENCY_FORMAT.format(consumption) + " " + unit;
    }
}
