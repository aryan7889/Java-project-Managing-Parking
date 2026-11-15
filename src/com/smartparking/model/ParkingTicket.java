package com.smartparking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a parking session record (ticket) generated when a vehicle enters the facility.
 * Tracks the vehicle reference, allocated slot, entry/exit timestamps, billed hours, fee, and status.
 */
public class ParkingTicket {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String ticketId;
    private Vehicle vehicle;
    private String slotId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private long durationHours;
    private double feeCharged;
    private TicketStatus status;

    /**
     * Parameterized constructor used upon vehicle entry.
     */
    public ParkingTicket(String ticketId, Vehicle vehicle, String slotId, LocalDateTime entryTime) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket ID cannot be empty.");
        }
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle cannot be null.");
        }
        if (slotId == null || slotId.trim().isEmpty()) {
            throw new IllegalArgumentException("Slot ID cannot be empty.");
        }
        if (entryTime == null) {
            throw new IllegalArgumentException("Entry time cannot be null.");
        }

        this.ticketId = ticketId.trim().toUpperCase();
        this.vehicle = vehicle;
        this.slotId = slotId.trim().toUpperCase();
        this.entryTime = entryTime;
        this.exitTime = null;
        this.durationHours = 0;
        this.feeCharged = 0.0;
        this.status = TicketStatus.ACTIVE;
    }

    /**
     * Full parameterized constructor (used when reconstructing tickets from storage or tests).
     */
    public ParkingTicket(String ticketId, Vehicle vehicle, String slotId,
                         LocalDateTime entryTime, LocalDateTime exitTime,
                         long durationHours, double feeCharged, TicketStatus status) {
        this(ticketId, vehicle, slotId, entryTime);
        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.feeCharged = feeCharged;
        this.status = status != null ? status : TicketStatus.ACTIVE;
    }

    /**
     * Completes the ticket upon vehicle checkout.
     *
     * @param exitTime      The timestamp when vehicle departs
     * @param durationHours Total billable hours (ceiling applied)
     * @param feeCharged    The final calculated parking fee
     */
    public void completeTicket(LocalDateTime exitTime, long durationHours, double feeCharged) {
        if (exitTime == null) {
            throw new IllegalArgumentException("Exit time cannot be null.");
        }
        if (durationHours < 1) {
            durationHours = 1;
        }
        if (feeCharged < 0) {
            throw new IllegalArgumentException("Fee cannot be negative.");
        }

        this.exitTime = exitTime;
        this.durationHours = durationHours;
        this.feeCharged = feeCharged;
        this.status = TicketStatus.COMPLETED;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getSlotId() {
        return slotId;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public long getDurationHours() {
        return durationHours;
    }

    public double getFeeCharged() {
        return feeCharged;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public String getFormattedEntryTime() {
        return entryTime != null ? entryTime.format(FORMATTER) : "N/A";
    }

    public String getFormattedExitTime() {
        return exitTime != null ? exitTime.format(FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return String.format(
                "Ticket [%s] | %s | Slot: %s | In: %s | Out: %s | Duration: %dh | Fee: ₹%.2f | Status: %s",
                ticketId,
                vehicle.getRegistrationNumber(),
                slotId,
                getFormattedEntryTime(),
                getFormattedExitTime(),
                durationHours,
                feeCharged,
                status
        );
    }
}
