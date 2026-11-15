package com.smartparking.service;

import com.smartparking.exception.DuplicateVehicleException;
import com.smartparking.exception.InvalidTicketException;
import com.smartparking.exception.SlotUnavailableException;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the physical layout, slot allocation, release, occupancy inspection,
 * vehicle entry/exit workflows, polymorphic billing, and parking history.
 */
public class ParkingLot {

    // Default slot capacities if not explicitly specified
    public static final int DEFAULT_BIKE_SLOTS = 5;
    public static final int DEFAULT_CAR_SLOTS = 10;
    public static final int DEFAULT_TRUCK_SLOTS = 3;

    private final List<ParkingSlot> slots;
    private final Map<String, ParkingTicket> activeTickets;
    private final List<ParkingTicket> parkingHistory;
    private int ticketCounter;

    /**
     * Default constructor: Initializes parking lot with default slot capacities
     * (5 Bike slots, 10 Car slots, 3 Truck slots).
     */
    public ParkingLot() {
        this(DEFAULT_BIKE_SLOTS, DEFAULT_CAR_SLOTS, DEFAULT_TRUCK_SLOTS);
    }

    /**
     * Parameterized constructor: Configures custom capacities for each vehicle type.
     *
     * @param bikeSlots  Number of dedicated bike slots
     * @param carSlots   Number of dedicated car slots
     * @param truckSlots Number of dedicated truck slots
     */
    public ParkingLot(int bikeSlots, int carSlots, int truckSlots) {
        if (bikeSlots < 1 || carSlots < 1 || truckSlots < 1) {
            throw new IllegalArgumentException("Slot counts must be at least 1 for each vehicle category.");
        }
        this.slots = new ArrayList<>();
        this.activeTickets = new LinkedHashMap<>();
        this.parkingHistory = new ArrayList<>();
        this.ticketCounter = 1001;
        initializeSlots(bikeSlots, carSlots, truckSlots);
    }

    /**
     * Generates structured slot IDs and populates the parking lot:
     * - Bike slots:  B-1, B-2, ...
     * - Car slots:   C-1, C-2, ...
     * - Truck slots: T-1, T-2, ...
     */
    private void initializeSlots(int bikeSlots, int carSlots, int truckSlots) {
        for (int i = 1; i <= bikeSlots; i++) {
            slots.add(new ParkingSlot("B-" + i, VehicleType.BIKE));
        }
        for (int i = 1; i <= carSlots; i++) {
            slots.add(new ParkingSlot("C-" + i, VehicleType.CAR));
        }
        for (int i = 1; i <= truckSlots; i++) {
            slots.add(new ParkingSlot("T-" + i, VehicleType.TRUCK));
        }
    }

    /**
     * Automatically searches for the first available vacant slot suitable for the given vehicle type.
     *
     * @param vehicleType The category of vehicle requiring parking
     * @return An unoccupied ParkingSlot, or null if all matching slots are occupied
     */
    public ParkingSlot findAvailableSlot(VehicleType vehicleType) {
        if (vehicleType == null) {
            return null;
        }
        for (ParkingSlot slot : slots) {
            if (slot.getSupportedType() == vehicleType && !slot.isOccupied()) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Allocates a suitable parking slot for an incoming vehicle.
     * Checks for duplicate vehicle occupancy before allocation.
     *
     * @param vehicle The vehicle to be parked
     * @return The allocated ParkingSlot
     * @throws DuplicateVehicleException If this vehicle is already parked inside the facility
     * @throws SlotUnavailableException  If no compatible vacant slot is found
     */
    public ParkingSlot allocateSlot(Vehicle vehicle) throws DuplicateVehicleException, SlotUnavailableException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Cannot allocate slot for null vehicle.");
        }

        // Check if vehicle is already parked in any slot or active ticket
        if (isVehicleParked(vehicle.getRegistrationNumber())) {
            ParkingSlot existingSlot = findSlotByVehicleNumber(vehicle.getRegistrationNumber());
            String slotId = existingSlot != null ? existingSlot.getSlotId() : "Active Ticket";
            throw new DuplicateVehicleException(String.format(
                    "Vehicle [%s] is already parked in slot [%s]. Duplicate entry not allowed.",
                    vehicle.getRegistrationNumber(),
                    slotId
            ));
        }

        // Find available slot for this vehicle type
        ParkingSlot suitableSlot = findAvailableSlot(vehicle.getVehicleType());
        if (suitableSlot == null) {
            throw new SlotUnavailableException(String.format(
                    "Sorry, no vacant parking slots available for [%s]. Parking lot is full for this vehicle category.",
                    vehicle.getVehicleType().getDisplayName()
            ));
        }

        // Assign vehicle to slot
        suitableSlot.assignVehicle(vehicle);
        return suitableSlot;
    }

    /**
     * Complete Vehicle Entry Workflow:
     * 1. Validates vehicle details.
     * 2. Checks duplicate active status.
     * 3. Finds and allocates suitable slot.
     * 4. Stamps entry timestamp.
     * 5. Generates unique parking ticket ID (e.g., TCK-1001).
     * 6. Registers the active parking ticket.
     *
     * @param vehicle The incoming vehicle
     * @return Newly created active ParkingTicket
     * @throws DuplicateVehicleException If vehicle is already active in lot
     * @throws SlotUnavailableException  If no compatible slot is available
     */
    public ParkingTicket parkVehicle(Vehicle vehicle) throws DuplicateVehicleException, SlotUnavailableException {
        if (vehicle == null) {
            throw new IllegalArgumentException("Cannot park null vehicle.");
        }

        // Allocate slot (throws DuplicateVehicleException or SlotUnavailableException if invalid)
        ParkingSlot allocatedSlot = allocateSlot(vehicle);

        // Generate unique ticket and capture entry timestamp
        String ticketId = generateNextTicketId();
        LocalDateTime entryTime = LocalDateTime.now();

        ParkingTicket ticket = new ParkingTicket(ticketId, vehicle, allocatedSlot.getSlotId(), entryTime);
        activeTickets.put(ticket.getTicketId(), ticket);

        return ticket;
    }

    /**
     * Complete Vehicle Exit Workflow using the current system time as exit time.
     *
     * @param identifier Ticket ID (e.g., "TCK-1001") or vehicle registration number (e.g., "MH-12-AB-1234")
     * @return Completed ParkingTicket with duration and calculated fee
     * @throws InvalidTicketException If no matching active parking record is found
     */
    public ParkingTicket checkoutVehicle(String identifier) throws InvalidTicketException {
        return checkoutVehicle(identifier, LocalDateTime.now());
    }

    /**
     * Complete Vehicle Exit Workflow with an explicit exit timestamp (enables deterministic testing).
     *
     * 1. Retrieves active parking ticket.
     * 2. Computes total duration in minutes.
     * 3. Applies the ceiling rule: rounds up partial hours (e.g., 1h 10m = 2 hrs; min 1 hr).
     * 4. Calculates fee polymorphically: vehicle.calculateFee(hours).
     * 5. Releases the occupied parking slot.
     * 6. Marks ticket status as COMPLETED.
     * 7. Moves ticket from active registry to parking history.
     *
     * @param identifier Ticket ID or vehicle registration number
     * @param exitTime   Timestamp of vehicle departure
     * @return Completed ParkingTicket with final billing
     * @throws InvalidTicketException If no active session matches the identifier
     */
    public ParkingTicket checkoutVehicle(String identifier, LocalDateTime exitTime) throws InvalidTicketException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new InvalidTicketException("Identifier cannot be empty. Please provide a ticket ID or vehicle number.");
        }

        String searchKey = identifier.trim().toUpperCase();

        // 1. Look up active ticket by ticket ID first, then by vehicle registration number
        ParkingTicket ticket = activeTickets.get(searchKey);
        if (ticket == null) {
            ticket = getActiveTicketByVehicleNumber(searchKey);
        }

        if (ticket == null) {
            throw new InvalidTicketException(String.format(
                    "No active parking ticket found for [%s]. The vehicle may not be parked or already checked out.",
                    identifier
            ));
        }

        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }

        // 2. Compute total duration in minutes
        long durationMinutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        if (durationMinutes < 0) {
            durationMinutes = 0;
        }

        // 3. Apply ceiling rule: any fraction of an hour is rounded up to the next full hour
        // Minimum duration is 1 hour
        long billableHours;
        if (durationMinutes <= 0) {
            billableHours = 1;
        } else {
            billableHours = (long) Math.ceil(durationMinutes / 60.0);
        }

        // 4. Calculate fee polymorphically via the Vehicle subclass
        double fee = ticket.getVehicle().calculateFee(billableHours);

        // 5. Complete the ticket details
        ticket.completeTicket(exitTime, billableHours, fee);

        // 6. Release the physical slot back to vacant
        releaseSlot(ticket.getSlotId());

        // 7. Remove from active sessions and archive into completed history
        activeTickets.remove(ticket.getTicketId());
        parkingHistory.add(ticket);

        return ticket;
    }

    /**
     * Generates a sequential, unique ticket identifier.
     */
    private synchronized String generateNextTicketId() {
        return "TCK-" + (ticketCounter++);
    }

    /**
     * Allows seeding the ticket counter (useful during data restoration).
     */
    public synchronized void setTicketCounter(int counter) {
        if (counter > this.ticketCounter) {
            this.ticketCounter = counter;
        }
    }

    /**
     * Reconstructs an active ticket session (used during startup persistence sync).
     */
    public void restoreActiveTicket(ParkingTicket ticket) {
        if (ticket == null || ticket.getStatus() != TicketStatus.ACTIVE) {
            return;
        }
        activeTickets.put(ticket.getTicketId(), ticket);
        ParkingSlot slot = findSlotById(ticket.getSlotId());
        if (slot != null && !slot.isOccupied()) {
            slot.assignVehicle(ticket.getVehicle());
        }
        // Ensure ticket counter stays above loaded tickets
        updateCounterFromTicketId(ticket.getTicketId());
    }

    /**
     * Reconstructs a historical ticket record (used during startup persistence sync).
     */
    public void restoreHistoricalTicket(ParkingTicket ticket) {
        if (ticket == null) {
            return;
        }
        parkingHistory.add(ticket);
        updateCounterFromTicketId(ticket.getTicketId());
    }

    private void updateCounterFromTicketId(String ticketId) {
        try {
            int numericPart = Integer.parseInt(ticketId.replace("TCK-", ""));
            if (numericPart >= ticketCounter) {
                ticketCounter = numericPart + 1;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Releases a parking slot by slot ID, resetting it to vacant.
     */
    public boolean releaseSlot(String slotId) {
        ParkingSlot slot = findSlotById(slotId);
        if (slot != null && slot.isOccupied()) {
            slot.releaseSlot();
            return true;
        }
        return false;
    }

    /**
     * Releases a parking slot assigned to a specific vehicle registration number.
     */
    public boolean releaseSlotForVehicle(String registrationNumber) {
        ParkingSlot slot = findSlotByVehicleNumber(registrationNumber);
        if (slot != null && slot.isOccupied()) {
            slot.releaseSlot();
            return true;
        }
        return false;
    }

    /**
     * Finds a parking slot by its slot ID.
     */
    public ParkingSlot findSlotById(String slotId) {
        if (slotId == null) {
            return null;
        }
        String normalizedId = slotId.trim().toUpperCase();
        for (ParkingSlot slot : slots) {
            if (slot.getSlotId().equalsIgnoreCase(normalizedId)) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Finds the parking slot currently occupied by a given vehicle registration number.
     */
    public ParkingSlot findSlotByVehicleNumber(String registrationNumber) {
        if (registrationNumber == null) {
            return null;
        }
        String normalizedReg = registrationNumber.trim().toUpperCase();
        for (ParkingSlot slot : slots) {
            if (slot.isOccupied() && slot.getAssignedVehicle() != null) {
                if (slot.getAssignedVehicle().getRegistrationNumber().equalsIgnoreCase(normalizedReg)) {
                    return slot;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a vehicle with the given registration number is currently parked.
     */
    public boolean isVehicleParked(String registrationNumber) {
        if (registrationNumber == null) {
            return false;
        }
        String normalized = registrationNumber.trim().toUpperCase();
        if (findSlotByVehicleNumber(normalized) != null) {
            return true;
        }
        return getActiveTicketByVehicleNumber(normalized) != null;
    }

    /**
     * Retrieves an active ticket by its ticket ID.
     */
    public ParkingTicket getActiveTicketById(String ticketId) {
        if (ticketId == null) {
            return null;
        }
        return activeTickets.get(ticketId.trim().toUpperCase());
    }

    /**
     * Retrieves an active ticket by vehicle registration number.
     */
    public ParkingTicket getActiveTicketByVehicleNumber(String registrationNumber) {
        if (registrationNumber == null) {
            return null;
        }
        String normalizedReg = registrationNumber.trim().toUpperCase();
        for (ParkingTicket ticket : activeTickets.values()) {
            if (ticket.getVehicle().getRegistrationNumber().equalsIgnoreCase(normalizedReg)) {
                return ticket;
            }
        }
        return null;
    }

    /**
     * Searches any ticket (active or completed history) by ticket ID.
     */
    public ParkingTicket findTicketById(String ticketId) {
        if (ticketId == null) {
            return null;
        }
        String normalized = ticketId.trim().toUpperCase();
        ParkingTicket active = activeTickets.get(normalized);
        if (active != null) {
            return active;
        }
        for (ParkingTicket historical : parkingHistory) {
            if (historical.getTicketId().equalsIgnoreCase(normalized)) {
                return historical;
            }
        }
        return null;
    }

    /**
     * Searches all tickets (active and completed history) for a given vehicle registration number.
     */
    public List<ParkingTicket> findTicketsByVehicleNumber(String registrationNumber) {
        List<ParkingTicket> result = new ArrayList<>();
        if (registrationNumber == null) {
            return result;
        }
        String normalized = registrationNumber.trim().toUpperCase();
        for (ParkingTicket active : activeTickets.values()) {
            if (active.getVehicle().getRegistrationNumber().equalsIgnoreCase(normalized)) {
                result.add(active);
            }
        }
        for (ParkingTicket historical : parkingHistory) {
            if (historical.getVehicle().getRegistrationNumber().equalsIgnoreCase(normalized)) {
                result.add(historical);
            }
        }
        return result;
    }

    /**
     * Returns an unmodifiable list of all currently active parking tickets.
     */
    public List<ParkingTicket> getActiveTickets() {
        return Collections.unmodifiableList(new ArrayList<>(activeTickets.values()));
    }

    /**
     * Returns an unmodifiable list of all completed parking history tickets.
     */
    public List<ParkingTicket> getParkingHistory() {
        return Collections.unmodifiableList(parkingHistory);
    }

    public int getActiveTicketCount() {
        return activeTickets.size();
    }

    public int getCompletedSessionsCount() {
        return parkingHistory.size();
    }

    public double getTotalRevenue() {
        double total = 0.0;
        for (ParkingTicket ticket : parkingHistory) {
            total += ticket.getFeeCharged();
        }
        return total;
    }

    /**
     * Returns an unmodifiable list of all parking slots.
     */
    public List<ParkingSlot> getAllSlots() {
        return Collections.unmodifiableList(slots);
    }

    /**
     * Returns a list of all currently vacant parking slots.
     */
    public List<ParkingSlot> getAvailableSlots() {
        List<ParkingSlot> available = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if (!slot.isOccupied()) {
                available.add(slot);
            }
        }
        return available;
    }

    /**
     * Returns a list of all currently occupied parking slots.
     */
    public List<ParkingSlot> getOccupiedSlots() {
        List<ParkingSlot> occupied = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if (slot.isOccupied()) {
                occupied.add(slot);
            }
        }
        return occupied;
    }

    /**
     * Returns slots filtered by vehicle type.
     */
    public List<ParkingSlot> getSlotsByType(VehicleType type) {
        List<ParkingSlot> matching = new ArrayList<>();
        for (ParkingSlot slot : slots) {
            if (slot.getSupportedType() == type) {
                matching.add(slot);
            }
        }
        return matching;
    }

    public int getTotalSlotsCount() {
        return slots.size();
    }

    public int getAvailableSlotsCount() {
        return getAvailableSlots().size();
    }

    public int getOccupiedSlotsCount() {
        return getOccupiedSlots().size();
    }

    /**
     * Formats and prints a formal entry pass receipt to the console.
     */
    public void printEntryPass(ParkingTicket ticket) {
        if (ticket == null) {
            return;
        }
        System.out.println("==================================================");
        System.out.println("          SMART PARKING - ENTRY PASS              ");
        System.out.println("==================================================");
        System.out.printf(" Ticket ID      : %s%n", ticket.getTicketId());
        System.out.printf(" Vehicle No     : %s%n", ticket.getVehicle().getRegistrationNumber());
        System.out.printf(" Vehicle Type   : %s (₹%.0f/hr)%n",
                ticket.getVehicle().getVehicleType().getDisplayName(),
                ticket.getVehicle().getVehicleType().getHourlyRate());
        System.out.printf(" Owner Name     : %s%n", ticket.getVehicle().getOwnerName());
        System.out.printf(" Assigned Slot  : %s%n", ticket.getSlotId());
        System.out.printf(" Entry Time     : %s%n", ticket.getFormattedEntryTime());
        System.out.printf(" Status         : %s%n", ticket.getStatus());
        System.out.println("==================================================");
        System.out.println("  * Please keep this ticket safe for checkout. *  ");
        System.out.println("==================================================");
    }

    /**
     * Formats and prints a detailed exit billing receipt to the console.
     */
    public void printCheckoutReceipt(ParkingTicket ticket) {
        if (ticket == null) {
            return;
        }
        long minutes = Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toMinutes();
        if (minutes < 0) minutes = 0;
        long hoursPart = minutes / 60;
        long minutesPart = minutes % 60;

        System.out.println("==================================================");
        System.out.println("          SMART PARKING - EXIT INVOICE            ");
        System.out.println("==================================================");
        System.out.printf(" Ticket ID       : %s%n", ticket.getTicketId());
        System.out.printf(" Vehicle No      : %s%n", ticket.getVehicle().getRegistrationNumber());
        System.out.printf(" Vehicle Type    : %s%n", ticket.getVehicle().getVehicleType().getDisplayName());
        System.out.printf(" Owner Name      : %s%n", ticket.getVehicle().getOwnerName());
        System.out.printf(" Released Slot   : %s%n", ticket.getSlotId());
        System.out.printf(" Entry Time      : %s%n", ticket.getFormattedEntryTime());
        System.out.printf(" Exit Time       : %s%n", ticket.getFormattedExitTime());
        System.out.printf(" Actual Duration : %d hr %d min (Total: %d mins)%n", hoursPart, minutesPart, minutes);
        System.out.printf(" Billed Duration : %d hour(s) (Ceiling Applied)%n", ticket.getDurationHours());
        System.out.printf(" Hourly Rate     : ₹%.2f/hr%n", ticket.getVehicle().getVehicleType().getHourlyRate());
        System.out.println("--------------------------------------------------");
        System.out.printf(" TOTAL AMOUNT DUE: ₹%.2f%n", ticket.getFeeCharged());
        System.out.printf(" Payment Status  : %s (PAID)%n", ticket.getStatus());
        System.out.println("==================================================");
        System.out.println("         Thank you for parking with us!           ");
        System.out.println("==================================================");
    }

    /**
     * Displays all completed parking sessions in a formatted table.
     */
    public void displayParkingHistory() {
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
        System.out.printf(" COMPLETED PARKING SESSIONS (Total: %d | Revenue: ₹%.2f)%n", parkingHistory.size(), getTotalRevenue());
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-7s | %-15s | %-6s | %-19s | %-19s | %-6s | %-9s%n",
                "Ticket ID", "Vehicle No", "Type", "Owner", "Slot", "Entry Time", "Exit Time", "Hours", "Fee (₹)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------");

        if (parkingHistory.isEmpty()) {
            System.out.println(" [No completed parking sessions recorded yet]");
        } else {
            for (ParkingTicket t : parkingHistory) {
                System.out.printf("%-10s | %-12s | %-7s | %-15s | %-6s | %-19s | %-19s | %-6d | ₹%-8.2f%n",
                        t.getTicketId(),
                        t.getVehicle().getRegistrationNumber(),
                        t.getVehicle().getVehicleType(),
                        t.getVehicle().getOwnerName(),
                        t.getSlotId(),
                        t.getFormattedEntryTime(),
                        t.getFormattedExitTime(),
                        t.getDurationHours(),
                        t.getFeeCharged());
            }
        }
        System.out.println("-------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Formats and prints a list of parking slots as a readable table.
     */
    public void printSlotTable(List<ParkingSlot> slotList, String headerTitle) {
        System.out.println("----------------------------------------------------------------------");
        System.out.printf(" %s (Total: %d)%n", headerTitle, slotList.size());
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("%-10s | %-10s | %-12s | %-20s%n", "Slot ID", "Type", "Status", "Vehicle / Owner");
        System.out.println("----------------------------------------------------------------------");

        if (slotList.isEmpty()) {
            System.out.println(" [No slots match this criteria]");
        } else {
            for (ParkingSlot slot : slotList) {
                String status = slot.isOccupied() ? "OCCUPIED" : "VACANT";
                String details = slot.isOccupied() && slot.getAssignedVehicle() != null
                        ? String.format("%s (%s)", slot.getAssignedVehicle().getRegistrationNumber(), slot.getAssignedVehicle().getOwnerName())
                        : "-";
                System.out.printf("%-10s | %-10s | %-12s | %-20s%n",
                        slot.getSlotId(),
                        slot.getSupportedType(),
                        status,
                        details);
            }
        }
        System.out.println("----------------------------------------------------------------------");
    }

    public void displayAllSlots() {
        printSlotTable(getAllSlots(), "ALL PARKING SLOTS");
    }

    public void displayAvailableSlots() {
        printSlotTable(getAvailableSlots(), "AVAILABLE (VACANT) PARKING SLOTS");
    }

    public void displayOccupiedSlots() {
        printSlotTable(getOccupiedSlots(), "OCCUPIED PARKING SLOTS");
    }
}
