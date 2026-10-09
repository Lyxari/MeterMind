package metermind.model;

/**
 * Enum representing the types of utility bills tracked by MeterMind.
 */
public enum UtilityType {
    ELECTRICITY("Electricity", "kWh"),
    WATER("Water", "m³");

    private final String displayName;
    private final String unit;

    UtilityType(String displayName, String unit) {
        this.displayName = displayName;
        this.unit = unit;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
