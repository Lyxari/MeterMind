package metermind.alerts;

/**
 * Enum representing alert severity levels.
 */
public enum AlertSeverity {
    INFO("Info", "#0EA5E9"),       // Blue/Teal
    WARNING("Warning", "#F59E0B"), // Amber
    DANGER("Danger", "#EF4444");   // Red

    private final String label;
    private final String colorHex;

    AlertSeverity(String label, String colorHex) {
        this.label = label;
        this.colorHex = colorHex;
    }

    public String getLabel() { return label; }
    public String getColorHex() { return colorHex; }
}
