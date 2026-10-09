package metermind.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regex-based parser for extracting bill data from pasted SMS or email text.
 * Attempts to identify consumption, amount, due date, and provider from raw text.
 */
public final class RegexParser {

    // Patterns for common Philippine utility bill notification formats
    private static final Pattern AMOUNT_PATTERN =
            Pattern.compile("(?:amount|total|due|bill)[:\\s]*(?:PHP|₱|Php)?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE);

    private static final Pattern CONSUMPTION_PATTERN =
            Pattern.compile("(?:consumption|usage|reading)[:\\s]*([\\d,]+\\.?\\d*)\\s*(?:kWh|kwh|m3|m³|cu\\.?\\s*m)?", Pattern.CASE_INSENSITIVE);

    private static final Pattern DUE_DATE_PATTERN =
            Pattern.compile("(?:due\\s*(?:date)?)[:\\s]*(\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}|\\w+\\s+\\d{1,2},?\\s*\\d{4})", Pattern.CASE_INSENSITIVE);

    private static final Pattern MONTH_YEAR_PATTERN =
            Pattern.compile("(?:for|period|month)[:\\s]*(\\w+)\\s*(\\d{4})", Pattern.CASE_INSENSITIVE);

    private static final Pattern PROVIDER_PATTERN =
            Pattern.compile("(Meralco|Maynilad|Manila\\s*Water|VECO|CEBECO|DLPC|MORE\\s*Power)", Pattern.CASE_INSENSITIVE);

    private static final Pattern UTILITY_TYPE_PATTERN =
            Pattern.compile("(electric|electricity|power|water)", Pattern.CASE_INSENSITIVE);

    private RegexParser() {
        // Utility class — no instantiation
    }

    /**
     * Attempts to extract bill data fields from raw pasted text.
     *
     * @param text Raw SMS, email, or notification text
     * @return Map of extracted field names to values. Keys may include:
     *         "amount", "consumption", "dueDate", "month", "year", "provider", "type"
     */
    public static Map<String, String> parse(String text) {
        Map<String, String> result = new HashMap<>();

        // Extract amount
        Matcher m = AMOUNT_PATTERN.matcher(text);
        if (m.find()) {
            result.put("amount", m.group(1).replace(",", ""));
        }

        // Extract consumption
        m = CONSUMPTION_PATTERN.matcher(text);
        if (m.find()) {
            result.put("consumption", m.group(1).replace(",", ""));
        }

        // Extract due date
        m = DUE_DATE_PATTERN.matcher(text);
        if (m.find()) {
            result.put("dueDate", m.group(1).trim());
        }

        // Extract billing period (month + year)
        m = MONTH_YEAR_PATTERN.matcher(text);
        if (m.find()) {
            result.put("month", m.group(1).trim());
            result.put("year", m.group(2).trim());
        }

        // Extract provider
        m = PROVIDER_PATTERN.matcher(text);
        if (m.find()) {
            result.put("provider", m.group(1).trim());
        }

        // Detect utility type
        m = UTILITY_TYPE_PATTERN.matcher(text);
        if (m.find()) {
            String matched = m.group(1).toLowerCase();
            if (matched.contains("water")) {
                result.put("type", "WATER");
            } else {
                result.put("type", "ELECTRICITY");
            }
        }

        return result;
    }

    /**
     * Resolves a month name string to a 1-based month number.
     * Handles full names (January) and abbreviations (Jan).
     *
     * @return month number 1–12, or -1 if unrecognized
     */
    public static int resolveMonth(String monthStr) {
        if (monthStr == null) return -1;
        String lower = monthStr.toLowerCase().trim();
        for (int i = 0; i < DateUtil.MONTH_NAMES.length; i++) {
            if (DateUtil.MONTH_NAMES[i].toLowerCase().startsWith(lower) ||
                lower.startsWith(DateUtil.MONTH_NAMES[i].toLowerCase().substring(0, 3))) {
                return i + 1;
            }
        }
        return -1;
    }
}
