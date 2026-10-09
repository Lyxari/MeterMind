package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;

import java.util.List;
import java.util.Optional;

/**
 * Compares the two most recent consecutive months for a given utility type.
 */
public class MonthOverMonth extends Comparison {

    @Override
    public Optional<GapResult> compare(BillHistory history, UtilityType type) {
        List<UtilityBill> bills = history.getByUtilityType(type);
        if (bills.size() < 2) {
            return Optional.empty();
        }

        // Bills are sorted chronologically — take the last two
        UtilityBill previous = bills.get(bills.size() - 2);
        UtilityBill current = bills.get(bills.size() - 1);

        return Optional.of(analyze(previous, current));
    }
}
