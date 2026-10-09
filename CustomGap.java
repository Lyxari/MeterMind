package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;

import java.util.Optional;

/**
 * Compares any two specific bills selected by year and month.
 */
public class CustomGap extends Comparison {

    private final int year1;
    private final int month1;
    private final int year2;
    private final int month2;

    public CustomGap(int year1, int month1, int year2, int month2) {
        this.year1 = year1;
        this.month1 = month1;
        this.year2 = year2;
        this.month2 = month2;
    }

    @Override
    public Optional<GapResult> compare(BillHistory history, UtilityType type) {
        Optional<UtilityBill> bill1 = history.getByYearMonth(year1, month1, type);
        Optional<UtilityBill> bill2 = history.getByYearMonth(year2, month2, type);

        if (bill1.isPresent() && bill2.isPresent()) {
            return Optional.of(analyze(bill1.get(), bill2.get()));
        }

        return Optional.empty();
    }
}
