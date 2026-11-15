package com.smartparking.exception;

/**
 * Thrown when an incoming vehicle cannot be accommodated because
 * all suitable parking slots for its vehicle type are fully occupied.
 */
public class SlotUnavailableException extends ParkingException {
    public SlotUnavailableException(String message) {
        super(message);
    }
}
