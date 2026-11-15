package com.smartparking;

import com.smartparking.exception.DuplicateVehicleException;
import com.smartparking.exception.InvalidTicketException;
import com.smartparking.exception.SlotUnavailableException;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.service.FileStorageService;
import com.smartparking.service.ParkingLot;
import com.smartparking.util.VehicleFactory;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application controller and entry point for the Smart Parking System.
 * Coordinates user interaction through a numbered console menu, orchestrates
 * ParkingLot operations, and synchronizes data with FileStorageService.
 */
public class ParkingManager {

    private final ParkingLot parkingLot;
    private final FileStorageService storageService;
    private final Scanner scanner;

    public ParkingManager() {
        this(new ParkingLot(), new FileStorageService());
    }

    public ParkingManager(ParkingLot parkingLot, FileStorageService storageService) {
        this.parkingLot = parkingLot;
        this.storageService = storageService;
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        ParkingManager app = new ParkingManager();
        app.start();
    }

    /**
     * Boots the application, loads persistent records, and runs the interactive menu loop.
     */
    public void start() {
        System.out.println("==================================================");
        System.out.println("    WELCOME TO SMART PARKING MANAGEMENT SYSTEM    ");
        System.out.println("==================================================");

        // 1. Restore persistent data from disk upon launch
        loadPersistedData();

        // 2. Main menu loop
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    handleVehicleEntry();
                    break;
                case 2:
                    handleVehicleExit();
                    break;
                case 3:
                    parkingLot.displayAllSlots();
                    break;
                case 4:
                    parkingLot.displayAvailableSlots();
                    break;
                case 5:
                    parkingLot.displayOccupiedSlots();
                    break;
                case 6:
                    handleSearchVehicle();
                    break;
                case 7:
                    handleSearchTicket();
                    break;
                case 8:
                    parkingLot.displayParkingHistory();
                    break;
                case 9:
                    handleDisplayStatistics();
                    break;
                case 10:
                    running = false;
                    handleExitApplication();
                    break;
                default:
                    System.out.println(" [!] Invalid option. Please enter a number between 1 and 10.");
            }
            if (running) {
                promptEnterToContinue();
            }
        }
    }

    private void displayMenu() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("       SMART PARKING & SLOT MANAGEMENT SYSTEM     ");
        System.out.println("==================================================");
        System.out.println("  1. Register Vehicle / Vehicle Entry");
        System.out.println("  2. Vehicle Exit & Billing");
        System.out.println("  3. Display All Parking Slots");
        System.out.println("  4. Display Available Slots");
        System.out.println("  5. Display Occupied Slots");
        System.out.println("  6. Search Vehicle");
        System.out.println("  7. Search Parking Ticket");
        System.out.println("  8. View Parking History");
        System.out.println("  9. Display Parking Statistics");
        System.out.println("  10. Exit Application");
        System.out.println("==================================================");
        System.out.print("Enter your choice (1-10): ");
    }

    /**
     * Restores state from files during application boot.
     */
    private void loadPersistedData() {
        System.out.println("[*] Initializing system and checking persistent storage...");
        List<ParkingTicket> active = storageService.loadActiveTickets();
        for (ParkingTicket ticket : active) {
            parkingLot.restoreActiveTicket(ticket);
        }

        List<ParkingTicket> history = storageService.loadParkingHistory();
        for (ParkingTicket ticket : history) {
            parkingLot.restoreHistoricalTicket(ticket);
        }

        System.out.printf("[✓] Data loaded successfully: %d active vehicle(s), %d completed session(s).%n",
                parkingLot.getActiveTicketCount(), parkingLot.getCompletedSessionsCount());
    }

    /**
     * Persists active tickets to file.
     */
    private void persistActiveTickets() {
        try {
            storageService.saveActiveTickets(parkingLot.getActiveTickets());
        } catch (IOException e) {
            System.err.println("[Error] Failed to save active tickets to disk: " + e.getMessage());
        }
    }

    /**
     * Appends completed ticket to history file.
     */
    private void persistHistoricalTicket(ParkingTicket ticket) {
        try {
            storageService.appendHistoricalTicket(ticket);
        } catch (IOException e) {
            System.err.println("[Error] Failed to append ticket to history file: " + e.getMessage());
        }
    }

    /**
     * Workflow 1: Vehicle Entry
     */
    private void handleVehicleEntry() {
        System.out.println("\n--- [Option 1] Vehicle Registration & Entry ---");

        String regNumber = readNonEmptyString("Enter Vehicle Registration Number (e.g. MH-12-AB-1234): ");
        if (regNumber == null) return;

        String ownerName = readNonEmptyString("Enter Vehicle Owner Name: ");
        if (ownerName == null) return;

        VehicleType type = readVehicleTypeChoice();
        if (type == null) {
            System.out.println(" [!] Vehicle entry cancelled.");
            return;
        }

        try {
            Vehicle vehicle = VehicleFactory.createVehicle(regNumber, ownerName, type);
            ParkingTicket ticket = parkingLot.parkVehicle(vehicle);

            // Auto-persist active tickets
            persistActiveTickets();

            System.out.println("\n [✓] Vehicle parked successfully!");
            parkingLot.printEntryPass(ticket);

        } catch (DuplicateVehicleException e) {
            System.out.println("\n [✗] REGISTRATION REJECTED: " + e.getMessage());
        } catch (SlotUnavailableException e) {
            System.out.println("\n [✗] PARKING FULL: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n [✗] ERROR: " + e.getMessage());
        }
    }

    /**
     * Workflow 2: Vehicle Exit
     */
    private void handleVehicleExit() {
        System.out.println("\n--- [Option 2] Vehicle Exit & Billing ---");
        String identifier = readNonEmptyString("Enter Ticket ID (e.g. TCK-1001) or Vehicle Registration Number: ");
        if (identifier == null) return;

        try {
            ParkingTicket ticket = parkingLot.checkoutVehicle(identifier);

            // Auto-persist active tickets and append to history
            persistActiveTickets();
            persistHistoricalTicket(ticket);

            System.out.println("\n [✓] Vehicle checkout completed successfully!");
            parkingLot.printCheckoutReceipt(ticket);

        } catch (InvalidTicketException e) {
            System.out.println("\n [✗] CHECKOUT FAILED: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n [✗] ERROR: " + e.getMessage());
        }
    }

    /**
     * Workflow 6: Search Vehicle
     */
    private void handleSearchVehicle() {
        System.out.println("\n--- [Option 6] Search Vehicle ---");
        String regNumber = readNonEmptyString("Enter Vehicle Registration Number to search: ");
        if (regNumber == null) return;

        List<ParkingTicket> tickets = parkingLot.findTicketsByVehicleNumber(regNumber);
        ParkingSlot currentSlot = parkingLot.findSlotByVehicleNumber(regNumber);

        System.out.println("--------------------------------------------------");
        System.out.printf(" Search Results for Vehicle: [%s]%n", regNumber.trim().toUpperCase());
        System.out.println("--------------------------------------------------");

        if (currentSlot != null && currentSlot.isOccupied()) {
            System.out.println(" CURRENT STATUS: Currently Parked inside facility");
            System.out.printf("  - Slot Assigned : %s (%s)%n", currentSlot.getSlotId(), currentSlot.getSupportedType());
            System.out.printf("  - Owner Name    : %s%n", currentSlot.getAssignedVehicle().getOwnerName());
        } else {
            System.out.println(" CURRENT STATUS: Not currently parked inside facility");
        }

        if (tickets.isEmpty()) {
            System.out.println(" No historical or active tickets found for this vehicle number.");
        } else {
            System.out.printf(" Ticket Records Found (%d):%n", tickets.size());
            for (ParkingTicket t : tickets) {
                System.out.printf("  * Ticket ID: %-9s | Status: %-9s | Slot: %-4s | In: %s | Out: %s | Fee: ₹%.2f%n",
                        t.getTicketId(),
                        t.getStatus(),
                        t.getSlotId(),
                        t.getFormattedEntryTime(),
                        t.getFormattedExitTime(),
                        t.getFeeCharged());
            }
        }
        System.out.println("--------------------------------------------------");
    }

    /**
     * Workflow 7: Search Parking Ticket
     */
    private void handleSearchTicket() {
        System.out.println("\n--- [Option 7] Search Parking Ticket ---");
        String ticketId = readNonEmptyString("Enter Ticket ID to search (e.g. TCK-1001): ");
        if (ticketId == null) return;

        ParkingTicket ticket = parkingLot.findTicketById(ticketId);
        if (ticket == null) {
            System.out.printf(" [!] No record found for Ticket ID [%s].%n", ticketId.trim().toUpperCase());
            return;
        }

        System.out.println("--------------------------------------------------");
        System.out.printf(" Ticket Details: [%s]%n", ticket.getTicketId());
        System.out.println("--------------------------------------------------");
        System.out.printf("  - Ticket Status   : %s%n", ticket.getStatus());
        System.out.printf("  - Vehicle Number  : %s%n", ticket.getVehicle().getRegistrationNumber());
        System.out.printf("  - Vehicle Type    : %s%n", ticket.getVehicle().getVehicleType());
        System.out.printf("  - Owner Name      : %s%n", ticket.getVehicle().getOwnerName());
        System.out.printf("  - Slot Assigned   : %s%n", ticket.getSlotId());
        System.out.printf("  - Entry Timestamp : %s%n", ticket.getFormattedEntryTime());
        System.out.printf("  - Exit Timestamp  : %s%n", ticket.getFormattedExitTime());
        System.out.printf("  - Billed Duration : %d hour(s)%n", ticket.getDurationHours());
        System.out.printf("  - Total Fee       : ₹%.2f%n", ticket.getFeeCharged());
        System.out.println("--------------------------------------------------");
    }

    /**
     * Workflow 9: Statistics
     */
    private void handleDisplayStatistics() {
        System.out.println("\n==================================================");
        System.out.println("          PARKING FACILITY STATISTICS             ");
        System.out.println("==================================================");
        System.out.printf(" Total Slots Capacity      : %d%n", parkingLot.getTotalSlotsCount());
        System.out.printf("  - Bike Slots             : %d%n", parkingLot.getSlotsByType(VehicleType.BIKE).size());
        System.out.printf("  - Car Slots              : %d%n", parkingLot.getSlotsByType(VehicleType.CAR).size());
        System.out.printf("  - Truck Slots            : %d%n", parkingLot.getSlotsByType(VehicleType.TRUCK).size());
        System.out.println("--------------------------------------------------");
        System.out.printf(" Available (Vacant) Slots  : %d%n", parkingLot.getAvailableSlotsCount());
        System.out.printf(" Occupied Slots            : %d%n", parkingLot.getOccupiedSlotsCount());
        System.out.println("--------------------------------------------------");
        System.out.printf(" Active Parking Sessions   : %d%n", parkingLot.getActiveTicketCount());
        System.out.printf(" Completed Sessions (Total): %d%n", parkingLot.getCompletedSessionsCount());
        System.out.printf(" Total Revenue Collected   : ₹%.2f%n", parkingLot.getTotalRevenue());
        System.out.println("==================================================");
    }

    private void handleExitApplication() {
        System.out.println("\n[*] Saving current state before exit...");
        persistActiveTickets();
        System.out.println("[✓] All parking data saved to disk.");
        System.out.println("==================================================");
        System.out.println(" Thank you for using Smart Parking System. Goodbye!");
        System.out.println("==================================================");
    }

    // =========================================================================
    // Robust Scanner & Input Utilities (Handles EOF and bad inputs gracefully)
    // =========================================================================

    private int readMenuChoice() {
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print(" [!] Please enter a valid number (1-10): ");
            }
        }
        return 10; // Default to Exit upon EOF
    }

    private String readNonEmptyString(String prompt) {
        System.out.print(prompt);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.print(" [!] Input cannot be blank. Please try again: ");
        }
        return null;
    }

    private VehicleType readVehicleTypeChoice() {
        System.out.println("Select Vehicle Type:");
        System.out.println(" 1. Bike  (₹10/hr)");
        System.out.println(" 2. Car   (₹20/hr)");
        System.out.println(" 3. Truck (₹40/hr)");
        System.out.print("Enter choice (1-3): ");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            if ("1".equals(input)) {
                return VehicleType.BIKE;
            } else if ("2".equals(input)) {
                return VehicleType.CAR;
            } else if ("3".equals(input)) {
                return VehicleType.TRUCK;
            } else {
                System.out.print(" [!] Invalid vehicle type. Enter 1 for Bike, 2 for Car, 3 for Truck: ");
            }
        }
        return null;
    }

    private void promptEnterToContinue() {
        System.out.println("\nPress [Enter] to return to the main menu...");
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public FileStorageService getStorageService() {
        return storageService;
    }
}
