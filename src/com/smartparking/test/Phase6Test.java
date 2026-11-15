package com.smartparking.test;

import com.smartparking.exception.DuplicateVehicleException;
import com.smartparking.exception.InvalidTicketException;
import com.smartparking.exception.SlotUnavailableException;
import com.smartparking.model.ParkingSlot;
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
 * Phase 6 End-to-End Integration Test Suite.
 * Explicitly verifies all 14 mandatory test scenarios outlined in the master specification:
 * 1. Successful vehicle entry.
 * 2. Duplicate vehicle entry.
 * 3. Parking lot full.
 * 4. Different vehicle types (Bike, Car, Truck).
 * 5. Successful vehicle exit.
 * 6. Invalid vehicle exit.
 * 7. Correct fee calculation.
 * 8. Partial-hour billing (ceiling rule).
 * 9. Slot release.
 * 10. Search by vehicle number.
 * 11. Search by ticket ID.
 * 12. Viewing parking history.
 * 13. Restarting the application and restoring saved data.
 * 14. Missing or corrupted files.
 */
public class Phase6Test {

    private static final String TEST_DATA_DIR = "phase6_test_data";
    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 6 COMPREHENSIVE INTEGRATION TEST  ");
        System.out.println("==================================================================");

        FileStorageService storage = new FileStorageService(TEST_DATA_DIR);
        storage.clearAllData();

        try {
            // Scenarios 1, 4: Successful Entry & Different Vehicle Types
            scenario1and4_SuccessfulEntryAndDifferentVehicleTypes();

            // Scenario 2: Duplicate Vehicle Entry Rejection
            scenario2_DuplicateVehicleEntry();

            // Scenario 3: Parking Lot Full Handling
            scenario3_ParkingLotFull();

            // Scenarios 5, 7, 8, 9: Exit, Correct Fee, Partial-Hour Billing, Slot Release
            scenarios5_7_8_9_ExitBillingCeilingAndSlotRelease();

            // Scenario 6: Invalid Vehicle Exit
            scenario6_InvalidVehicleExit();

            // Scenario 10: Search by Vehicle Number
            scenario10_SearchByVehicleNumber();

            // Scenario 11: Search by Ticket ID
            scenario11_SearchByTicketId();

            // Scenario 12: Viewing Parking History & Revenue
            scenario12_ViewingParkingHistory();

            // Scenario 13: Restarting Application & Data Restoration
            scenario13_RestartingAndRestoringData(storage);

            // Scenario 14: Missing or Corrupted Files Recovery
            scenario14_MissingOrCorruptedFiles();

        } finally {
            storage.clearAllData();
            new File(TEST_DATA_DIR).delete();
        }

        System.out.println("==================================================================");
        System.out.printf("PHASE 6 TEST RESULTS: %d/%d Passed%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL 14 SCENARIOS PASSED SUCCESSFULLY! ✓");
        } else {
            System.err.println("STATUS: SOME SCENARIOS FAILED. PLEASE REVIEW LOGS. ✗");
        }
        System.out.println("==================================================================");
    }

    private static void assertTrue(boolean condition, String scenarioName) {
        totalTests++;
        if (condition) {
            System.out.println("  [PASS] " + scenarioName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + scenarioName);
        }
    }

    // Scenario 1 & 4
    private static void scenario1and4_SuccessfulEntryAndDifferentVehicleTypes() {
        System.out.println("\n--- [Scenario 1 & 4]: Successful Entry & Different Vehicle Types ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle bike = VehicleFactory.createVehicle("MH-12-BK-100", "Bike Rider", VehicleType.BIKE);
            Vehicle car = VehicleFactory.createVehicle("MH-12-CR-200", "Car Driver", VehicleType.CAR);
            Vehicle truck = VehicleFactory.createVehicle("MH-12-TR-300", "Truck Driver", VehicleType.TRUCK);

            ParkingTicket tBike = lot.parkVehicle(bike);
            ParkingTicket tCar = lot.parkVehicle(car);
            ParkingTicket tTruck = lot.parkVehicle(truck);

            assertTrue("B-1".equals(tBike.getSlotId()), "Bike parked in dedicated Bike slot B-1");
            assertTrue("C-1".equals(tCar.getSlotId()), "Car parked in dedicated Car slot C-1");
            assertTrue("T-1".equals(tTruck.getSlotId()), "Truck parked in dedicated Truck slot T-1");

            assertTrue(lot.getActiveTicketCount() == 3, "All 3 vehicle types parked actively");

        } catch (Exception e) {
            assertTrue(false, "Entry failed: " + e.getMessage());
        }
    }

    // Scenario 2
    private static void scenario2_DuplicateVehicleEntry() {
        System.out.println("\n--- [Scenario 2]: Duplicate Vehicle Entry Prevention ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle car = VehicleFactory.createVehicle("DL-01-DUP-1", "Same Driver", VehicleType.CAR);
            lot.parkVehicle(car); // First entry

            boolean caughtDuplicate = false;
            try {
                lot.parkVehicle(car); // Second attempt
            } catch (DuplicateVehicleException e) {
                caughtDuplicate = true;
            }
            assertTrue(caughtDuplicate, "System rejects re-entry of currently parked vehicle (DuplicateVehicleException)");

        } catch (Exception e) {
            assertTrue(false, "Unexpected error: " + e.getMessage());
        }
    }

    // Scenario 3
    private static void scenario3_ParkingLotFull() {
        System.out.println("\n--- [Scenario 3]: Parking Lot Full Situation ---");

        ParkingLot lot = new ParkingLot(1, 1, 1);
        try {
            Vehicle bike1 = VehicleFactory.createVehicle("WB-01-B1", "Rider 1", VehicleType.BIKE);
            Vehicle bike2 = VehicleFactory.createVehicle("WB-01-B2", "Rider 2", VehicleType.BIKE);

            lot.parkVehicle(bike1); // Fills only Bike slot B-1

            boolean caughtFull = false;
            try {
                lot.parkVehicle(bike2); // Rejection
            } catch (SlotUnavailableException e) {
                caughtFull = true;
            }
            assertTrue(caughtFull, "System cleanly rejects entry when category slots are full (SlotUnavailableException)");

        } catch (Exception e) {
            assertTrue(false, "Unexpected error: " + e.getMessage());
        }
    }

    // Scenarios 5, 7, 8, 9
    private static void scenarios5_7_8_9_ExitBillingCeilingAndSlotRelease() {
        System.out.println("\n--- [Scenarios 5, 7, 8, 9]: Exit, Correct Fee, Ceiling Billing & Slot Release ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle car = VehicleFactory.createVehicle("KA-05-CR-777", "Ravi", VehicleType.CAR);
            ParkingTicket ticket = lot.parkVehicle(car); // Takes C-1

            assertTrue(lot.findSlotById("C-1").isOccupied(), "Slot C-1 occupied before exit");

            // Parked for 1 hour 10 minutes (70 mins) -> rounds up to 2 hours
            LocalDateTime exitTime = ticket.getEntryTime().plusMinutes(70);
            ParkingTicket checkout = lot.checkoutVehicle(ticket.getTicketId(), exitTime);

            assertTrue(checkout.getStatus() == TicketStatus.COMPLETED, "Scenario 5: Vehicle exit marked COMPLETED");
            assertTrue(checkout.getDurationHours() == 2, "Scenario 8: 70 mins rounded to 2 hours (Ceiling Rule)");
            assertTrue(checkout.getFeeCharged() == 40.0, "Scenario 7: Correct polymorphic fee ₹40.0 (2 hrs * ₹20)");
            assertTrue(!lot.findSlotById("C-1").isOccupied(), "Scenario 9: Slot C-1 released and vacant immediately");

        } catch (Exception e) {
            assertTrue(false, "Exit/Billing error: " + e.getMessage());
        }
    }

    // Scenario 6
    private static void scenario6_InvalidVehicleExit() {
        System.out.println("\n--- [Scenario 6]: Invalid Vehicle Exit Handling ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);

        // Checkout non-existent ticket
        boolean caughtInvalid = false;
        try {
            lot.checkoutVehicle("TCK-9999");
        } catch (InvalidTicketException e) {
            caughtInvalid = true;
        }
        assertTrue(caughtInvalid, "Checkout of non-existent ticket throws InvalidTicketException");

        // Checkout already-checked-out vehicle
        try {
            Vehicle car = VehicleFactory.createVehicle("UP-16-EXIT", "Ramesh", VehicleType.CAR);
            ParkingTicket t = lot.parkVehicle(car);
            lot.checkoutVehicle(t.getTicketId());

            boolean caughtDoubleCheckout = false;
            try {
                lot.checkoutVehicle(t.getTicketId());
            } catch (InvalidTicketException e) {
                caughtDoubleCheckout = true;
            }
            assertTrue(caughtDoubleCheckout, "Checkout of already completed ticket throws InvalidTicketException");

        } catch (Exception e) {
            assertTrue(false, "Unexpected error: " + e.getMessage());
        }
    }

    // Scenario 10
    private static void scenario10_SearchByVehicleNumber() {
        System.out.println("\n--- [Scenario 10]: Search by Vehicle Registration Number ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle bike = VehicleFactory.createVehicle("TN-01-SEARCH-1", "Kavita", VehicleType.BIKE);
            lot.parkVehicle(bike);

            // Search by exact plate
            ParkingTicket ticket = lot.getActiveTicketByVehicleNumber("TN-01-SEARCH-1");
            assertTrue(ticket != null, "Vehicle found by exact registration number");

            // Search case-insensitively
            ParkingTicket ticketCase = lot.getActiveTicketByVehicleNumber("tn-01-search-1");
            assertTrue(ticketCase != null, "Vehicle search is case-insensitive");

            // Search history & active tickets collection
            List<ParkingTicket> allTickets = lot.findTicketsByVehicleNumber("tn-01-search-1");
            assertTrue(allTickets.size() == 1, "findTicketsByVehicleNumber returns ticket list");

        } catch (Exception e) {
            assertTrue(false, "Search error: " + e.getMessage());
        }
    }

    // Scenario 11
    private static void scenario11_SearchByTicketId() {
        System.out.println("\n--- [Scenario 11]: Search by Ticket ID ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle car = VehicleFactory.createVehicle("RJ-14-TICK-1", "Alok", VehicleType.CAR);
            ParkingTicket ticket = lot.parkVehicle(car);

            ParkingTicket found = lot.findTicketById(ticket.getTicketId());
            assertTrue(found != null && found.equals(ticket), "findTicketById locates active ticket");

            // Checkout and search history
            lot.checkoutVehicle(ticket.getTicketId());
            ParkingTicket foundHistorical = lot.findTicketById(ticket.getTicketId());
            assertTrue(foundHistorical != null && foundHistorical.getStatus() == TicketStatus.COMPLETED,
                    "findTicketById locates completed ticket in history");

        } catch (Exception e) {
            assertTrue(false, "Ticket search error: " + e.getMessage());
        }
    }

    // Scenario 12
    private static void scenario12_ViewingParkingHistory() {
        System.out.println("\n--- [Scenario 12]: Viewing Parking History & Revenue ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        try {
            Vehicle bike = VehicleFactory.createVehicle("MH-04-BK-1", "User 1", VehicleType.BIKE);
            Vehicle car = VehicleFactory.createVehicle("MH-04-CR-2", "User 2", VehicleType.CAR);

            ParkingTicket tb = lot.parkVehicle(bike);
            ParkingTicket tc = lot.parkVehicle(car);

            lot.checkoutVehicle(tb.getTicketId(), tb.getEntryTime().plusHours(1)); // ₹10
            lot.checkoutVehicle(tc.getTicketId(), tc.getEntryTime().plusHours(2)); // ₹40

            assertTrue(lot.getCompletedSessionsCount() == 2, "Parking history contains 2 completed sessions");
            assertTrue(lot.getTotalRevenue() == 50.0, "Total historical revenue equals ₹50.0");

        } catch (Exception e) {
            assertTrue(false, "History error: " + e.getMessage());
        }
    }

    // Scenario 13
    private static void scenario13_RestartingAndRestoringData(FileStorageService storage) {
        System.out.println("\n--- [Scenario 13]: Application Restart & Data Restoration ---");

        ParkingLot session1 = new ParkingLot(2, 2, 2);
        try {
            Vehicle truck = VehicleFactory.createVehicle("NL-01-TRUCK-9", "North Hauler", VehicleType.TRUCK);
            Vehicle car = VehicleFactory.createVehicle("NL-01-CAR-8", "South Cruiser", VehicleType.CAR);

            ParkingTicket tTruck = session1.parkVehicle(truck); // Active
            ParkingTicket tCar = session1.parkVehicle(car);
            session1.checkoutVehicle(tCar.getTicketId()); // Completed

            // Persist
            storage.saveActiveTickets(session1.getActiveTickets());
            storage.saveParkingHistory(session1.getParkingHistory());

            // Destroy Session 1
            session1 = null;

            // Boot Session 2
            ParkingLot session2 = new ParkingLot(2, 2, 2);
            for (ParkingTicket t : storage.loadActiveTickets()) {
                session2.restoreActiveTicket(t);
            }
            for (ParkingTicket t : storage.loadParkingHistory()) {
                session2.restoreHistoricalTicket(t);
            }

            assertTrue(session2.getActiveTicketCount() == 1, "Session 2 restored 1 active ticket");
            assertTrue(session2.isVehicleParked("NL-01-TRUCK-9"), "Truck is recognized as parked in Session 2");
            assertTrue(session2.findSlotById("T-1").isOccupied(), "Slot T-1 correctly restored as OCCUPIED");
            assertTrue(session2.getCompletedSessionsCount() == 1, "Session 2 restored 1 completed historical ticket");

        } catch (Exception e) {
            assertTrue(false, "Restart error: " + e.getMessage());
        }
    }

    // Scenario 14
    private static void scenario14_MissingOrCorruptedFiles() {
        System.out.println("\n--- [Scenario 14]: Missing or Corrupted Files Handling ---");

        String tempFolder = "temp_scen14_" + System.currentTimeMillis();
        FileStorageService testStorage = new FileStorageService(tempFolder);

        try {
            // Missing files test
            List<ParkingTicket> missingActive = testStorage.loadActiveTickets();
            List<ParkingTicket> missingHistory = testStorage.loadParkingHistory();
            assertTrue(missingActive.isEmpty() && missingHistory.isEmpty(),
                    "Missing files handled cleanly without application crash");

            // Corrupted file test
            File actFile = testStorage.getActiveTicketsFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(actFile))) {
                bw.write("CORRUPT_TOKEN_HEADER_LINE_WITHOUT_DELIMITERS\n");
                bw.write("TCK-9001|CAR|KA-01-SAFE-1|Safe Driver|C-1|2026-11-14T15:00:00\n");
            }

            List<ParkingTicket> restored = testStorage.loadActiveTickets();
            assertTrue(restored.size() == 1, "Corrupted line safely skipped; valid record successfully restored");
            assertTrue("TCK-9001".equals(restored.get(0).getTicketId()), "Restored record has ID TCK-9001");

        } catch (Exception e) {
            assertTrue(false, "Scenario 14 error: " + e.getMessage());
        } finally {
            testStorage.clearAllData();
            new File(tempFolder).delete();
        }
    }
}
