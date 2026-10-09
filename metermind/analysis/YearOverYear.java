package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;

import java.util.Optional;

/**
 * Compares the same month across two consecutive years.
 * Example: August 2025 vs August 2026.
 */
public class YearOverYear extends Comparison {

    private final int year1;
    private final int year2;
    private final int month;

    /**
     * @param year1 The earlier year
     * @param year2 The later year
     * @param month The month to compare (1–12)
     */
    public YearOverYear(int year1, int year2, int month) {
        this.year1 = year1;
        this.year2 = year2;
        this.month = month;
    }

    @Override
    public Optional<GapResult> compare(BillHistory history, UtilityType type) {
        Optional<UtilityBill> bill1 = history.getByYearMonth(year1, month, type);
        Optional<UtilityBill> bill2 = history.getByYearMonth(year2, month, type);

        if (bill1.isPresent() && bill2.isPresent()) {
            return Optional.of(analyze(bill1.get(), bill2.get()));
        }

        return Optional.empty();
    }
}
