package com.smartparking.exception;

/**
 * Thrown when an attempt is made to allocate a slot or register entry
 * for a vehicle that already has an active session in the parking lot.
 */
public class DuplicateVehicleException extends ParkingException {
    public DuplicateVehicleException(String message) {
        super(message);
    }
}
