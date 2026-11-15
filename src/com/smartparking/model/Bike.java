package com.smartparking.model;

/**
 * Concrete class representing a two-wheeler Bike.
 * Extends Vehicle and implements polymorphic fee calculation at ₹10/hour.
 */
public class Bike extends Vehicle {
    public static final double HOURLY_RATE = 10.0;

    public Bike(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.BIKE);
    }

    /**
     * Polymorphic implementation of fee calculation for Bikes.
     * Hourly Rate: ₹10/hour.
     */
    @Override
    public double calculateFee(long durationHours) {
        if (durationHours <= 0) {
            durationHours = 1; // Minimum billing duration is 1 hour
        }
        return durationHours * HOURLY_RATE;
    }
}
