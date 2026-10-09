package metermind.analysis;

import metermind.model.BillHistory;
import metermind.model.UtilityBill;
import metermind.model.UtilityType;

import java.util.Optional;

/**
 * Abstract base class for bill comparison strategies.
 * Subclasses define how two bills are selected for comparison.
 */
public abstract class Comparison {

    /**
     * Performs a gap analysis between two bills selected by this strategy.
     *
     * @param history The bill history to search
     * @param type    The utility type to compare
     * @return GapResult if both bills are found, empty otherwise
     */
    public abstract Optional<GapResult> compare(BillHistory history, UtilityType type);

    /**
     * Helper: creates a GapResult from two bills.
     */
    protected GapResult analyze(UtilityBill bill1, UtilityBill bill2) {
        return new GapResult(bill1, bill2);
    }
}
