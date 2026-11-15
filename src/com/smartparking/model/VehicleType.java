package com.smartparking.model;

/**
 * Enumeration representing the supported types of vehicles in the smart parking system.
 * Using an enum prevents invalid string values and ensures compile-time type safety.
 */
public enum VehicleType {
    BIKE("Bike", 10.0),
    CAR("Car", 20.0),
    TRUCK("Truck", 40.0);

    private final String displayName;
    private final double hourlyRate;

    VehicleType(String displayName, double hourlyRate) {
        this.displayName = displayName;
        this.hourlyRate = hourlyRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
