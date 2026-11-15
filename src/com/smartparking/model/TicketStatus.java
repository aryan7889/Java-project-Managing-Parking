package com.smartparking.model;

/**
 * Enumeration representing the lifecycle state of a parking ticket.
 * ACTIVE: The vehicle is currently parked inside the lot.
 * COMPLETED: The vehicle has exited, fee is paid, and the session is closed.
 */
public enum TicketStatus {
    ACTIVE,
    COMPLETED
}
