package com.smartparking.exception;

/**
 * Thrown when a ticket ID or vehicle number lookup fails, or when attempting
 * an operation on an already completed/non-existent parking ticket.
 */
public class InvalidTicketException extends ParkingException {
    public InvalidTicketException(String message) {
        super(message);
    }
}
