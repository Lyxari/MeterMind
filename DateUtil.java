package metermind.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date parsing, formatting, and relative date calculations.
 */
public final class DateUtil {

    /** Standard date format used for storage and display. */
    public static final DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** Display-friendly format. */
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /** Month names for UI display. */
    public static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    /** Short month names for charts and compact display. */
    public static final String[] MONTH_SHORT = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private DateUtil() {
        // Utility class — no instantiation
    }

    /**
     * Parses a date string in yyyy-MM-dd format.
     * Returns null if the string is invalid.
     */
    public static LocalDate parse(String dateStr) {
        try {
            return LocalDate.parse(dateStr, STANDARD_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats a LocalDate to yyyy-MM-dd for storage.
     */
    public static String format(LocalDate date) {
        return date.format(STANDARD_FORMAT);
    }

    /**
     * Formats a LocalDate for user-friendly display (e.g., "Oct 09, 2026").
     */
    public static String formatDisplay(LocalDate date) {
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * Returns the number of days until the due date.
     * Positive = due in the future, Negative = overdue.
     */
    public static long daysUntilDue(LocalDate dueDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }

    /**
     * Returns a human-readable string describing the due status.
     * Examples: "Due in 3 days", "Due tomorrow", "Due today", "Overdue by 2 days"
     */
    public static String getDueStatus(LocalDate dueDate) {
        long days = daysUntilDue(dueDate);
        if (days > 1) return "Due in " + days + " days";
        if (days == 1) return "Due tomorrow";
        if (days == 0) return "Due today";
        if (days == -1) return "Overdue by 1 day";
        return "Overdue by " + Math.abs(days) + " days";
    }

    /**
     * Returns the full month name for a 1-based month number.
     */
    public static String getMonthName(int month) {
        if (month < 1 || month > 12) return "Unknown";
        return MONTH_NAMES[month - 1];
    }

    /**
     * Returns the short month name for a 1-based month number.
     */
    public static String getMonthShort(int month) {
        if (month < 1 || month > 12) return "???";
        return MONTH_SHORT[month - 1];
    }

    /**
     * Formats a year-month pair as a readable label.
     * Example: 2026, 8 → "Aug 2026"
     */
    public static String formatYearMonth(int year, int month) {
        return getMonthShort(month) + " " + year;
    }
}
