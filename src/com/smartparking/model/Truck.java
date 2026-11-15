package com.smartparking.model;

/**
 * Concrete class representing a heavy vehicle Truck.
 * Extends Vehicle and implements polymorphic fee calculation at ₹40/hour.
 */
public class Truck extends Vehicle {
    public static final double HOURLY_RATE = 40.0;

    public Truck(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.TRUCK);
    }

    /**
     * Polymorphic implementation of fee calculation for Trucks.
     * Hourly Rate: ₹40/hour.
     */
    @Override
    public double calculateFee(long durationHours) {
        if (durationHours <= 0) {
            durationHours = 1; // Minimum billing duration is 1 hour
        }
        return durationHours * HOURLY_RATE;
    }
}
