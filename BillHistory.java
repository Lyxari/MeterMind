package metermind.model;

import metermind.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository class that manages a collection of utility bills.
 * Provides duplicate detection, filtering, sorting, and statistical queries.
 */
public class BillHistory {

    private final List<UtilityBill> bills;

    public BillHistory() {
        this.bills = new ArrayList<>();
    }

    /**
     * Adds a bill to the history after checking for duplicates.
     * A duplicate is defined as having the same year, month, and utility type.
     *
     * @throws DuplicateBillException if a bill for the same period and type already exists
     */
    public void addBill(UtilityBill bill) throws DuplicateBillException {
        for (UtilityBill existing : bills) {
            if (existing.getYear() == bill.getYear()
                    && existing.getMonth() == bill.getMonth()
                    && existing.getUtilityType() == bill.getUtilityType()) {
                throw new DuplicateBillException(
                        String.format("A %s bill for %d-%02d already exists.",
                                bill.getUtilityType().getDisplayName(), bill.getYear(), bill.getMonth()));
            }
        }
        bills.add(bill);
    }

    /**
     * Removes a bill by its unique ID.
     * @return true if a bill was removed, false if no bill matched the ID
     */
    public boolean removeBill(String id) {
        return bills.removeIf(b -> b.getId().equals(id));
    }

    /**
     * Updates an existing bill's data. The bill is matched by ID.
     * @return true if the bill was found and updated
     */
    public boolean updateBill(UtilityBill updatedBill) {
        for (int i = 0; i < bills.size(); i++) {
            if (bills.get(i).getId().equals(updatedBill.getId())) {
                bills.set(i, updatedBill);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a bill by its unique ID.
     */
    public Optional<UtilityBill> findById(String id) {
        return bills.stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    /**
     * Returns all bills sorted chronologically.
     */
    public List<UtilityBill> getAllSorted() {
        List<UtilityBill> sorted = new ArrayList<>(bills);
        Collections.sort(sorted);
        return sorted;
    }

    /**
     * Filters bills by utility type, sorted chronologically.
     */
    public List<UtilityBill> getByUtilityType(UtilityType type) {
        return bills.stream()
                .filter(b -> b.getUtilityType() == type)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Filters bills by year, sorted chronologically.
     */
    public List<UtilityBill> getByYear(int year) {
        return bills.stream()
                .filter(b -> b.getYear() == year)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Finds a specific bill by year, month, and utility type.
     */
    public Optional<UtilityBill> getByYearMonth(int year, int month, UtilityType type) {
        return bills.stream()
                .filter(b -> b.getYear() == year && b.getMonth() == month && b.getUtilityType() == type)
                .findFirst();
    }

    /**
     * Returns the most recent bill of a given utility type.
     */
    public Optional<UtilityBill> getLatestBill(UtilityType type) {
        return bills.stream()
                .filter(b -> b.getUtilityType() == type)
                .sorted(Collections.reverseOrder())
                .findFirst();
    }

    /**
     * Calculates average consumption for the last N bills of a given utility type.
     * Returns 0 if no bills match.
     */
    public double getAverageConsumption(UtilityType type, int lastN) {
        List<UtilityBill> typed = getByUtilityType(type);
        if (typed.isEmpty()) return 0;

        int start = Math.max(0, typed.size() - lastN);
        double sum = 0;
        int count = 0;
        for (int i = start; i < typed.size(); i++) {
            sum += typed.get(i).getConsumption();
            count++;
        }
        return count > 0 ? CurrencyUtil.round2(sum / count) : 0;
    }

    /**
     * Calculates average amount due for the last N bills of a given utility type.
     * Returns 0 if no bills match.
     */
    public double getAverageAmount(UtilityType type, int lastN) {
        List<UtilityBill> typed = getByUtilityType(type);
        if (typed.isEmpty()) return 0;

        int start = Math.max(0, typed.size() - lastN);
        double sum = 0;
        int count = 0;
        for (int i = start; i < typed.size(); i++) {
            sum += typed.get(i).getAmountDue();
            count++;
        }
        return count > 0 ? CurrencyUtil.round2(sum / count) : 0;
    }

    /**
     * Returns all distinct years present in the bill history.
     */
    public List<Integer> getAvailableYears() {
        return bills.stream()
                .map(UtilityBill::getYear)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns all distinct providers present in the bill history.
     */
    public List<String> getAvailableProviders() {
        return bills.stream()
                .map(UtilityBill::getProvider)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Returns total spending for a given year and utility type.
     */
    public double getTotalSpending(int year, UtilityType type) {
        return CurrencyUtil.round2(
                bills.stream()
                        .filter(b -> b.getYear() == year && b.getUtilityType() == type)
                        .mapToDouble(UtilityBill::getAmountDue)
                        .sum()
        );
    }

    /**
     * Returns total spending across all utility types for a given year and month.
     */
    public double getMonthlyTotal(int year, int month) {
        return CurrencyUtil.round2(
                bills.stream()
                        .filter(b -> b.getYear() == year && b.getMonth() == month)
                        .mapToDouble(UtilityBill::getAmountDue)
                        .sum()
        );
    }

    /**
     * Returns all unpaid bills, sorted chronologically.
     */
    public List<UtilityBill> getUnpaidBills() {
        return bills.stream()
                .filter(b -> !b.isPaid())
                .sorted()
                .collect(Collectors.toList());
    }

    public int size() {
        return bills.size();
    }

    public boolean isEmpty() {
        return bills.isEmpty();
    }
}
