package com.smartparking.model;

/**
 * Concrete class representing a four-wheeler Car.
 * Extends Vehicle and implements polymorphic fee calculation at ₹20/hour.
 */
public class Car extends Vehicle {
    public static final double HOURLY_RATE = 20.0;

    public Car(String registrationNumber, String ownerName) {
        super(registrationNumber, ownerName, VehicleType.CAR);
    }

    /**
     * Polymorphic implementation of fee calculation for Cars.
     * Hourly Rate: ₹20/hour.
     */
    @Override
    public double calculateFee(long durationHours) {
        if (durationHours <= 0) {
            durationHours = 1; // Minimum billing duration is 1 hour
        }
        return durationHours * HOURLY_RATE;
    }
}
