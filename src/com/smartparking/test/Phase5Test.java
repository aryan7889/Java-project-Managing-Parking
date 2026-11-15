package com.smartparking.test;

import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.service.FileStorageService;
import com.smartparking.service.ParkingLot;
import com.smartparking.util.VehicleFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 5 Test Program.
 * Verifies FileStorageService text persistence, active ticket restoration,
 * slot occupancy synchronization across application restarts, parking history
 * preservation, and graceful handling of missing, empty, or corrupted files.
 */
public class Phase5Test {

    private static final String TEST_DATA_DIR = "test_data";
    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 5 TEST SUITE      ");
        System.out.println("==================================================");

        FileStorageService storage = new FileStorageService(TEST_DATA_DIR);
        storage.clearAllData();

        try {
            testActiveTicketsSaveAndLoad(storage);
            testParkingHistoryAppendAndLoad(storage);
            testApplicationRestartSimulation(storage);
            testMissingAndEmptyFilesGracefulHandling();
            testMalformedLineGracefulHandling();
        } finally {
            // Clean up test files
            storage.clearAllData();
            new File(TEST_DATA_DIR).delete();
        }

        System.out.println("==================================================");
        System.out.printf("PHASE 5 TEST RESULTS: %d/%d Passed%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL PHASE 5 TESTS PASSED SUCCESSFULLY! ✓");
        } else {
            System.err.println("STATUS: SOME TESTS FAILED. PLEASE REVIEW LOGS. ✗");
        }
        System.out.println("==================================================");
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testActiveTicketsSaveAndLoad(FileStorageService storage) {
        System.out.println("\n--- Test Suite 1: Active Tickets Save & Load ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle car = VehicleFactory.createVehicle("MH-12-QQ-1111", "Amit Kumar", VehicleType.CAR);
            Vehicle bike = VehicleFactory.createVehicle("MH-12-QQ-2222", "Pooja Hegde", VehicleType.BIKE);

            lot.parkVehicle(car);
            lot.parkVehicle(bike);

            // Save active tickets
            storage.saveActiveTickets(lot.getActiveTickets());
            assertTrue(storage.getActiveTicketsFile().exists(), "active_tickets.txt file created on disk");

            // Load active tickets back
            List<ParkingTicket> loaded = storage.loadActiveTickets();
            assertTrue(loaded.size() == 2, "Loaded exactly 2 active tickets from file");

            ParkingTicket t1 = loaded.get(0);
            assertTrue("TCK-1001".equals(t1.getTicketId()), "Restored ticket ID is TCK-1001");
            assertTrue("MH-12-QQ-1111".equals(t1.getVehicle().getRegistrationNumber()), "Restored vehicle plate matches");
            assertTrue("C-1".equals(t1.getSlotId()), "Restored assigned slot is C-1");
            assertTrue(t1.getStatus() == TicketStatus.ACTIVE, "Restored ticket status is ACTIVE");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testParkingHistoryAppendAndLoad(FileStorageService storage) {
        System.out.println("\n--- Test Suite 2: Parking History Append & Load ---");

        try {
            Vehicle truck = VehicleFactory.createVehicle("KA-01-TR-9000", "Transport Corp", VehicleType.TRUCK);
            LocalDateTime inTime = LocalDateTime.of(2026, 11, 14, 8, 0, 0);
            LocalDateTime outTime = LocalDateTime.of(2026, 11, 14, 11, 0, 0); // 3 hours

            ParkingTicket histTicket = new ParkingTicket(
                    "TCK-1000", truck, "T-1", inTime, outTime, 3, 120.0, TicketStatus.COMPLETED
            );

            storage.appendHistoricalTicket(histTicket);
            assertTrue(storage.getParkingHistoryFile().exists(), "parking_history.txt created on disk");

            List<ParkingTicket> loadedHistory = storage.loadParkingHistory();
            assertTrue(loadedHistory.size() == 1, "Loaded 1 completed ticket from history");

            ParkingTicket loaded = loadedHistory.get(0);
            assertTrue("TCK-1000".equals(loaded.getTicketId()), "Historical ticket ID is TCK-1000");
            assertTrue(loaded.getDurationHours() == 3, "Historical duration is 3 hours");
            assertTrue(loaded.getFeeCharged() == 120.0, "Historical fee is ₹120.0");
            assertTrue(loaded.getStatus() == TicketStatus.COMPLETED, "Historical status is COMPLETED");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testApplicationRestartSimulation(FileStorageService storage) {
        System.out.println("\n--- Test Suite 3: Application Restart Simulation ---");

        // 1. Session 1: Create ParkingLot A, park 2 vehicles, checkout 1, save state
        ParkingLot session1Lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle car = VehicleFactory.createVehicle("DL-01-CR-8888", "Gaurav", VehicleType.CAR);
            Vehicle bike = VehicleFactory.createVehicle("DL-01-BK-7777", "Sneha", VehicleType.BIKE);

            ParkingTicket carTicket = session1Lot.parkVehicle(car); // Takes C-1, TCK-1001
            ParkingTicket bikeTicket = session1Lot.parkVehicle(bike); // Takes B-1, TCK-1002

            // Checkout the bike after 1 hour
            ParkingTicket completedBike = session1Lot.checkoutVehicle(bikeTicket.getTicketId(), bikeTicket.getEntryTime().plusHours(1));

            // Persist current state
            storage.saveActiveTickets(session1Lot.getActiveTickets());
            storage.appendHistoricalTicket(completedBike);

            // 2. Simulate complete application restart: destroy Session 1 references
            session1Lot = null;

            // 3. Session 2: Launch brand new ParkingLot instance
            ParkingLot session2Lot = new ParkingLot(2, 2, 2);

            // Initially session 2 has 0 active tickets and all slots vacant
            assertTrue(session2Lot.getActiveTicketCount() == 0, "Fresh ParkingLot starts with 0 active tickets");
            assertTrue(session2Lot.getAvailableSlotsCount() == 6, "Fresh ParkingLot starts with all slots vacant");

            // Load and restore persisted data into session2Lot
            List<ParkingTicket> restoredActive = storage.loadActiveTickets();
            for (ParkingTicket t : restoredActive) {
                session2Lot.restoreActiveTicket(t);
            }
            List<ParkingTicket> restoredHistory = storage.loadParkingHistory();
            for (ParkingTicket t : restoredHistory) {
                session2Lot.restoreHistoricalTicket(t);
            }

            // 4. Verify restored state integrity
            assertTrue(session2Lot.getActiveTicketCount() == 1, "Exactly 1 active ticket restored");
            assertTrue(session2Lot.isVehicleParked("DL-01-CR-8888"), "Restored car is recognized as parked");
            assertTrue(session2Lot.findSlotById("C-1").isOccupied(), "Slot C-1 is restored as OCCUPIED");
            assertTrue(!session2Lot.findSlotById("B-1").isOccupied(), "Slot B-1 remains VACANT (bike checked out)");

            // 5. Test slot consistency: Try to park a new car in Session 2
            Vehicle newCar = VehicleFactory.createVehicle("DL-01-CR-9999", "Harsh", VehicleType.CAR);
            ParkingTicket newTicket = session2Lot.parkVehicle(newCar);

            assertTrue("C-2".equals(newTicket.getSlotId()), "New car gets next vacant slot C-2 (not already-occupied C-1)");
            assertTrue("TCK-1003".equals(newTicket.getTicketId()), "New ticket ID resumes sequence correctly as TCK-1003");

            // 6. Test checkout of restored car
            ParkingTicket checkoutRestored = session2Lot.checkoutVehicle("TCK-1001", carTicket.getEntryTime().plusHours(2));
            assertTrue(checkoutRestored.getFeeCharged() == 40.0, "Restored car checked out for ₹40.0");
            assertTrue(!session2Lot.findSlotById("C-1").isOccupied(), "Slot C-1 is now released and VACANT");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception during restart simulation: " + e.getMessage());
        }
    }

    private static void testMissingAndEmptyFilesGracefulHandling() {
        System.out.println("\n--- Test Suite 4: Missing & Empty Files Graceful Handling ---");

        String nonExistentDir = "temp_non_existent_" + System.currentTimeMillis();
        FileStorageService emptyStorage = new FileStorageService(nonExistentDir);

        try {
            // Loading from non-existent files should return empty lists without crashing
            List<ParkingTicket> active = emptyStorage.loadActiveTickets();
            assertTrue(active.isEmpty(), "Missing active file returns clean empty list");

            List<ParkingTicket> history = emptyStorage.loadParkingHistory();
            assertTrue(history.isEmpty(), "Missing history file returns clean empty list");

            // Create empty files and test
            emptyStorage.getActiveTicketsFile().createNewFile();
            emptyStorage.getParkingHistoryFile().createNewFile();

            List<ParkingTicket> emptyActive = emptyStorage.loadActiveTickets();
            assertTrue(emptyActive.isEmpty(), "0-byte active file returns clean empty list");

            List<ParkingTicket> emptyHistory = emptyStorage.loadParkingHistory();
            assertTrue(emptyHistory.isEmpty(), "0-byte history file returns clean empty list");

        } catch (Exception e) {
            assertTrue(false, "Exception thrown on missing/empty files: " + e.getMessage());
        } finally {
            emptyStorage.clearAllData();
            new File(nonExistentDir).delete();
        }
    }

    private static void testMalformedLineGracefulHandling() {
        System.out.println("\n--- Test Suite 5: Malformed File Lines Graceful Handling ---");

        String corruptDir = "temp_corrupt_" + System.currentTimeMillis();
        FileStorageService corruptStorage = new FileStorageService(corruptDir);

        try {
            // Write a file with comments, empty lines, a corrupted line, and a valid line
            File file = corruptStorage.getActiveTicketsFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("# This is a header comment line");
                writer.newLine();
                writer.write(""); // Empty line
                writer.newLine();
                writer.write("INVALID_CORRUPT_DATA_WITHOUT_PROPER_DELIMITERS");
                writer.newLine();
                // Valid record
                writer.write("TCK-2001|CAR|MH-02-AA-9999|Good Citizen|C-1|2026-11-14T12:00:00");
                writer.newLine();
            }

            System.out.println("  [Testing graceful recovery from corrupt line]:");
            List<ParkingTicket> tickets = corruptStorage.loadActiveTickets();

            assertTrue(tickets.size() == 1, "Corrupt line skipped; exactly 1 valid ticket loaded");
            assertTrue("TCK-2001".equals(tickets.get(0).getTicketId()), "Valid ticket TCK-2001 parsed successfully");

        } catch (Exception e) {
            assertTrue(false, "Exception thrown during corrupt line handling: " + e.getMessage());
        } finally {
            corruptStorage.clearAllData();
            new File(corruptDir).delete();
        }
    }
}
