package metermind.alerts;

import metermind.model.*;
import metermind.util.DateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import javax.swing.SwingUtilities;

/**
 * Background alert manager that periodically checks for alert conditions
 * and notifies the UI. Runs on a daemon ScheduledExecutorService.
 */
public class AlertManager {

    private final BillHistory history;
    private final Budget budget;
    private final List<Alert> activeAlerts;
    private final List<AlertListener> listeners;
    private ScheduledExecutorService scheduler;

    private double highBillThreshold = 20.0; // Percentage above average to trigger alert
    private int reminderDays = 3;            // Days before due date to send reminder

    public AlertManager(BillHistory history, Budget budget) {
        this.history = history;
        this.budget = budget;
        this.activeAlerts = new CopyOnWriteArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
    }

    /**
     * Listener interface for UI components to receive alert updates.
     */
    public interface AlertListener {
        void onAlertsUpdated(List<Alert> alerts);
    }

    public void addListener(AlertListener listener) {
        listeners.add(listener);
    }

    public void removeListener(AlertListener listener) {
        listeners.remove(listener);
    }

    /**
     * Starts the background alert checker. Runs every 60 seconds.
     */
    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return; // Already running
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MeterMind-AlertChecker");
            t.setDaemon(true);
            return t;
        });

        // Run immediately, then every 60 seconds
        scheduler.scheduleAtFixedRate(this::checkAlerts, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * Stops the background alert checker.
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    /**
     * Manually triggers an alert check (can be called from UI after data changes).
     */
    public void checkAlerts() {
        List<Alert> newAlerts = new ArrayList<>();

        // Check for high bills
        checkHighBills(newAlerts);

        // Check for budget overages
        checkBudget(newAlerts);

        // Check for due date reminders
        checkDueDates(newAlerts);

        // Update active alerts
        synchronized (activeAlerts) {
            activeAlerts.clear();
            activeAlerts.addAll(newAlerts);
        }

        // Notify listeners on the EDT
        notifyListeners();
    }

    private void checkHighBills(List<Alert> alerts) {
        for (UtilityType type : UtilityType.values()) {
            List<UtilityBill> bills = history.getByUtilityType(type);
            if (bills.size() < 4) continue; // Need at least 4 bills (3 for avg + 1 current)

            UtilityBill latest = bills.get(bills.size() - 1);
            double avg = history.getAverageAmount(type, 3);

            if (avg > 0) {
                double threshold = avg * (1 + highBillThreshold / 100.0);
                if (latest.getAmountDue() > threshold) {
                    alerts.add(new HighBillAlert(latest, avg, highBillThreshold));
                }
            }
        }
    }

    private void checkBudget(List<Alert> alerts) {
        if (!budget.hasAnyLimit()) return;

        java.time.LocalDate now = java.time.LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Check per-utility budgets
        for (UtilityType type : UtilityType.values()) {
            double limit = budget.getLimitForType(type);
            if (limit > 0) {
                double spent = history.getTotalSpending(year, type);
                if (spent > limit) {
                    alerts.add(new BudgetAlert(type, spent, limit));
                }
            }
        }

        // Check overall budget
        double overallLimit = budget.getOverallMonthlyLimit();
        if (overallLimit > 0) {
            double totalSpent = history.getMonthlyTotal(year, month);
            if (totalSpent > overallLimit) {
                alerts.add(new BudgetAlert(null, totalSpent, overallLimit));
            }
        }
    }

    private void checkDueDates(List<Alert> alerts) {
        List<UtilityBill> unpaid = history.getUnpaidBills();
        for (UtilityBill bill : unpaid) {
            long daysUntil = DateUtil.daysUntilDue(bill.getDueDate());
            if (daysUntil <= reminderDays) {
                alerts.add(new DueDateReminder(bill));
            }
        }
    }

    private void notifyListeners() {
        List<Alert> snapshot;
        synchronized (activeAlerts) {
            snapshot = new ArrayList<>(activeAlerts);
        }

        SwingUtilities.invokeLater(() -> {
            for (AlertListener listener : listeners) {
                listener.onAlertsUpdated(snapshot);
            }
        });
    }

    // --- Configuration ---

    public void setHighBillThreshold(double percent) { this.highBillThreshold = percent; }
    public double getHighBillThreshold() { return highBillThreshold; }

    public void setReminderDays(int days) { this.reminderDays = days; }
    public int getReminderDays() { return reminderDays; }

    public List<Alert> getActiveAlerts() {
        synchronized (activeAlerts) {
            return new ArrayList<>(activeAlerts);
        }
    }

    public int getActiveAlertCount() {
        synchronized (activeAlerts) {
            return activeAlerts.size();
        }
    }
}
