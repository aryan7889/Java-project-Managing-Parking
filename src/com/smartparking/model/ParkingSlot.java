package com.smartparking.model;

/**
 * Represents a physical parking slot within the parking facility.
 * Demonstrates encapsulation by maintaining the slot's occupancy state
 * and ensuring only compatible vehicles are assigned.
 */
public class ParkingSlot {
    private String slotId;
    private VehicleType supportedType;
    private boolean occupied;
    private Vehicle assignedVehicle;

    /**
     * Constructs a new ParkingSlot.
     *
     * @param slotId        Unique slot identifier (e.g., "B1", "C1", "T1")
     * @param supportedType The vehicle type category this slot accommodates
     */
    public ParkingSlot(String slotId, VehicleType supportedType) {
        if (slotId == null || slotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Slot ID cannot be empty.");
        }
        if (supportedType == null) {
            throw new IllegalArgumentException("Supported vehicle type cannot be null.");
        }
        this.slotId = slotId.trim().toUpperCase();
        this.supportedType = supportedType;
        this.occupied = false;
        this.assignedVehicle = null;
    }

    /**
     * Assigns a vehicle to this slot if vacant and type-compatible.
     *
     * @param vehicle The vehicle to park
     * @return true if assignment succeeded, false otherwise
     */
    public boolean assignVehicle(Vehicle vehicle) {
        if (this.occupied) {
            return false;
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle to assign cannot be null.");
        }
        if (vehicle.getVehicleType() != this.supportedType) {
            return false;
        }
        this.assignedVehicle = vehicle;
        this.occupied = true;
        return true;
    }

    /**
     * Releases this slot, returning it to vacant status.
     */
    public void releaseSlot() {
        this.assignedVehicle = null;
        this.occupied = false;
    }

    public String getSlotId() {
        return slotId;
    }

    public VehicleType getSupportedType() {
        return supportedType;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Vehicle getAssignedVehicle() {
        return assignedVehicle;
    }

    @Override
    public String toString() {
        String status = occupied
                ? String.format("OCCUPIED by [%s]", assignedVehicle.getRegistrationNumber())
                : "VACANT";
        return String.format("Slot %-4s | Type: %-5s | Status: %s", slotId, supportedType, status);
    }
}
