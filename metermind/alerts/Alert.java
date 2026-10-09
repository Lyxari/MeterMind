package metermind.alerts;

import java.time.LocalDate;

/**
 * Abstract base class for all MeterMind alerts.
 */
public abstract class Alert {

    private final LocalDate createdDate;
    private boolean dismissed;

    protected Alert() {
        this.createdDate = LocalDate.now();
        this.dismissed = false;
    }

    /**
     * Returns the severity level of this alert.
     */
    public abstract AlertSeverity getSeverity();

    /**
     * Returns the alert message to display to the user.
     */
    public abstract String getMessage();

    /**
     * Returns a short title for the alert.
     */
    public abstract String getTitle();

    public LocalDate getCreatedDate() { return createdDate; }
    public boolean isDismissed() { return dismissed; }
    public void dismiss() { this.dismissed = true; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getSeverity().getLabel(), getTitle(), getMessage());
    }
}
