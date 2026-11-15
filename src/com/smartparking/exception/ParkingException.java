package com.smartparking.exception;

/**
 * Base custom exception for the Smart Parking System.
 * Inherits from java.lang.Exception (checked exception), ensuring calling
 * methods explicitly handle or declare parking-related operational failures.
 */
public class ParkingException extends Exception {
    public ParkingException(String message) {
        super(message);
    }

    public ParkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
