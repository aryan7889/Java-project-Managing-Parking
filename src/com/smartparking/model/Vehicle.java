package com.smartparking.model;

/**
 * Abstract base class representing a generic vehicle.
 * Demonstrates:
 * 1. Abstraction: Defines common attributes and declares the abstract method calculateFee().
 * 2. Encapsulation: Fields are private, accessible only through public getters and setters.
 */
public abstract class Vehicle {
    private String registrationNumber;
    private String ownerName;
    private VehicleType vehicleType;

    /**
     * Parameterized constructor to initialize common vehicle details.
     *
     * @param registrationNumber The unique vehicle registration plate number (e.g., MH-12-AB-1234)
     * @param ownerName          The name of the vehicle owner
     * @param vehicleType        The type category (BIKE, CAR, TRUCK)
     */
    public Vehicle(String registrationNumber, String ownerName, VehicleType vehicleType) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
        this.ownerName = ownerName.trim();
        this.vehicleType = vehicleType;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number cannot be empty.");
        }
        this.registrationNumber = registrationNumber.trim().toUpperCase();
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty.");
        }
        this.ownerName = ownerName.trim();
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    protected void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    /**
     * Abstract method to calculate parking fee based on duration in hours.
     * Must be implemented by concrete subclasses (Bike, Car, Truck) using polymorphism.
     *
     * @param durationHours Total parking duration in rounded hours (minimum 1)
     * @return Calculated fee amount in INR
     */
    public abstract double calculateFee(long durationHours);

    @Override
    public String toString() {
        return String.format("[%s] Reg: %s | Owner: %s", vehicleType, registrationNumber, ownerName);
    }
}
