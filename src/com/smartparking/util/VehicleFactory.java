package com.smartparking.util;

import com.smartparking.model.Bike;
import com.smartparking.model.Car;
import com.smartparking.model.Truck;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;

/**
 * Utility factory for creating concrete Vehicle instances safely.
 * Centralizes input validation and vehicle instantiation.
 */
public class VehicleFactory {

    /**
     * Creates and validates a concrete Vehicle instance (Bike, Car, or Truck).
     *
     * @param registrationNumber The vehicle plate number (e.g., "MH-12-AB-1234")
     * @param ownerName          The name of the vehicle owner
     * @param type               The vehicle type (BIKE, CAR, TRUCK)
     * @return Concrete Vehicle subclass instance
     * @throws IllegalArgumentException if inputs are null, empty, or invalid
     */
    public static Vehicle createVehicle(String registrationNumber, String ownerName, VehicleType type) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must be specified.");
        }

        String normalizedReg = registrationNumber.trim().toUpperCase();
        String normalizedOwner = ownerName.trim();

        switch (type) {
            case BIKE:
                return new Bike(normalizedReg, normalizedOwner);
            case CAR:
                return new Car(normalizedReg, normalizedOwner);
            case TRUCK:
                return new Truck(normalizedReg, normalizedOwner);
            default:
                throw new IllegalArgumentException("Unsupported vehicle type: " + type);
        }
    }
}
