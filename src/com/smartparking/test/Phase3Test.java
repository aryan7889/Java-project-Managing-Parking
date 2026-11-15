package com.smartparking.test;

import com.smartparking.exception.DuplicateVehicleException;
import com.smartparking.exception.SlotUnavailableException;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.service.ParkingLot;
import com.smartparking.util.VehicleFactory;

/**
 * Phase 3 Test Program.
 * Verifies Vehicle Entry workflow, input validation, unique ticket generation,
 * active session tracking, duplicate registration prevention, and entry receipt printing.
 */
public class Phase3Test {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 3 TEST SUITE      ");
        System.out.println("==================================================");

        testVehicleFactoryValidation();
        testSuccessfulVehicleEntryAndTicketGeneration();
        testSequentialTicketIdGeneration();
        testActiveTicketLookups();
        testDuplicateActiveVehicleEntryRejection();
        testSafeFailureWhenLotFull();
        testEntryPassFormatting();

        System.out.println("==================================================");
        System.out.printf("PHASE 3 TEST RESULTS: %d/%d Passed%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL PHASE 3 TESTS PASSED SUCCESSFULLY! ✓");
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

    private static void testVehicleFactoryValidation() {
        System.out.println("\n--- Test Suite 1: Vehicle Factory & Input Validation ---");

        Vehicle validCar = VehicleFactory.createVehicle("mh-12-ab-1234", "Vikram Rathore", VehicleType.CAR);
        assertTrue("MH-12-AB-1234".equals(validCar.getRegistrationNumber()), "Factory normalizes reg number to uppercase");
        assertTrue("Vikram Rathore".equals(validCar.getOwnerName()), "Factory properly sets owner name");
        assertTrue(validCar.getVehicleType() == VehicleType.CAR, "Factory correctly instantiates Car");

        // Reject empty reg number
        boolean caughtEmptyReg = false;
        try {
            VehicleFactory.createVehicle("   ", "Owner", VehicleType.BIKE);
        } catch (IllegalArgumentException e) {
            caughtEmptyReg = true;
        }
        assertTrue(caughtEmptyReg, "Factory rejects blank registration number");

        // Reject null owner name
        boolean caughtNullOwner = false;
        try {
            VehicleFactory.createVehicle("MH-12-1111", null, VehicleType.BIKE);
        } catch (IllegalArgumentException e) {
            caughtNullOwner = true;
        }
        assertTrue(caughtNullOwner, "Factory rejects null owner name");

        // Reject null vehicle type
        boolean caughtNullType = false;
        try {
            VehicleFactory.createVehicle("MH-12-1111", "Owner", null);
        } catch (IllegalArgumentException e) {
            caughtNullType = true;
        }
        assertTrue(caughtNullType, "Factory rejects null vehicle type");
    }

    private static void testSuccessfulVehicleEntryAndTicketGeneration() {
        System.out.println("\n--- Test Suite 2: Successful Entry & Ticket Generation ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = VehicleFactory.createVehicle("DL-08-CA-1001", "Meera Sen", VehicleType.CAR);

        try {
            ParkingTicket ticket = lot.parkVehicle(car);

            assertTrue(ticket != null, "Ticket generated upon entry");
            assertTrue("TCK-1001".equals(ticket.getTicketId()), "Initial ticket has ID TCK-1001");
            assertTrue("C-1".equals(ticket.getSlotId()), "Car allocated slot C-1");
            assertTrue(ticket.getStatus() == TicketStatus.ACTIVE, "Ticket status is ACTIVE");
            assertTrue(ticket.getEntryTime() != null, "Entry timestamp captured");
            assertTrue(lot.isVehicleParked("DL-08-CA-1001"), "Lot recognizes car as parked");
            assertTrue(lot.getActiveTicketCount() == 1, "Active ticket count is 1");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception during entry: " + e.getMessage());
        }
    }

    private static void testSequentialTicketIdGeneration() {
        System.out.println("\n--- Test Suite 3: Sequential Unique Ticket IDs ---");

        ParkingLot lot = new ParkingLot(3, 3, 3);
        Vehicle bike1 = VehicleFactory.createVehicle("MH-01-B1", "Owner 1", VehicleType.BIKE);
        Vehicle bike2 = VehicleFactory.createVehicle("MH-01-B2", "Owner 2", VehicleType.BIKE);
        Vehicle truck1 = VehicleFactory.createVehicle("MH-01-T1", "Owner 3", VehicleType.TRUCK);

        try {
            ParkingTicket t1 = lot.parkVehicle(bike1);
            ParkingTicket t2 = lot.parkVehicle(bike2);
            ParkingTicket t3 = lot.parkVehicle(truck1);

            assertTrue("TCK-1001".equals(t1.getTicketId()), "First ticket ID is TCK-1001");
            assertTrue("TCK-1002".equals(t2.getTicketId()), "Second ticket ID is TCK-1002");
            assertTrue("TCK-1003".equals(t3.getTicketId()), "Third ticket ID is TCK-1003");
            assertTrue(lot.getActiveTicketCount() == 3, "Total active tickets is 3");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testActiveTicketLookups() {
        System.out.println("\n--- Test Suite 4: Active Ticket Lookups (ID & Reg Number) ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = VehicleFactory.createVehicle("KA-03-MG-5555", "Suresh Nair", VehicleType.CAR);

        try {
            ParkingTicket createdTicket = lot.parkVehicle(car);

            // Lookup by exact ticket ID
            ParkingTicket foundById = lot.getActiveTicketById("TCK-1001");
            assertTrue(foundById != null && foundById.equals(createdTicket), "Ticket found by ticket ID TCK-1001");

            // Case-insensitive lookup by ticket ID
            ParkingTicket foundCaseInsensitive = lot.getActiveTicketById("tck-1001");
            assertTrue(foundCaseInsensitive != null, "Lookup by ticket ID is case-insensitive");

            // Lookup by registration number
            ParkingTicket foundByReg = lot.getActiveTicketByVehicleNumber("ka-03-mg-5555");
            assertTrue(foundByReg != null && "TCK-1001".equals(foundByReg.getTicketId()),
                    "Ticket found by case-insensitive vehicle registration number");

            // Lookup non-existent ticket
            assertTrue(lot.getActiveTicketById("TCK-9999") == null, "Non-existent ticket ID returns null");
            assertTrue(lot.getActiveTicketByVehicleNumber("XX-00-XX-0000") == null,
                    "Non-existent vehicle number returns null");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testDuplicateActiveVehicleEntryRejection() {
        System.out.println("\n--- Test Suite 5: Duplicate Vehicle Entry Prevention ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = VehicleFactory.createVehicle("MH-20-EE-9999", "Anil Kumble", VehicleType.CAR);

        try {
            lot.parkVehicle(car); // First entry succeeds
        } catch (Exception e) {
            assertTrue(false, "First entry should succeed");
        }

        boolean caughtDuplicate = false;
        try {
            // Attempt to park the same vehicle a second time
            lot.parkVehicle(car);
        } catch (DuplicateVehicleException e) {
            caughtDuplicate = true;
            System.out.println("  [Expected Exception Caught]: " + e.getMessage());
        } catch (Exception e) {
            caughtDuplicate = false;
        }

        assertTrue(caughtDuplicate, "parkVehicle rejects already parked vehicle with DuplicateVehicleException");
        assertTrue(lot.getActiveTicketCount() == 1, "Active ticket count unchanged after duplicate attempt");
    }

    private static void testSafeFailureWhenLotFull() {
        System.out.println("\n--- Test Suite 6: Safe Rejection When Lot Is Full ---");

        // Lot with only 1 Bike slot
        ParkingLot lot = new ParkingLot(1, 1, 1);
        Vehicle bike1 = VehicleFactory.createVehicle("WB-01-BK-11", "Rina", VehicleType.BIKE);
        Vehicle bike2 = VehicleFactory.createVehicle("WB-01-BK-22", "Kajal", VehicleType.BIKE);

        try {
            lot.parkVehicle(bike1); // Occupies B-1
        } catch (Exception e) {
            assertTrue(false, "Bike 1 should park successfully");
        }

        boolean caughtFull = false;
        try {
            lot.parkVehicle(bike2); // Fails
        } catch (SlotUnavailableException e) {
            caughtFull = true;
            System.out.println("  [Expected Exception Caught]: " + e.getMessage());
        } catch (Exception e) {
            caughtFull = false;
        }

        assertTrue(caughtFull, "SlotUnavailableException thrown when bike slots are full");
        assertTrue(lot.getActiveTicketCount() == 1, "No orphan ticket created on failure");
        assertTrue(lot.getActiveTicketByVehicleNumber("WB-01-BK-22") == null, "Rejected vehicle not in active tickets");
    }

    private static void testEntryPassFormatting() {
        System.out.println("\n--- Test Suite 7: Entry Pass Display Verification ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = VehicleFactory.createVehicle("RJ-14-CC-7777", "Deepak Joshi", VehicleType.CAR);

        try {
            ParkingTicket ticket = lot.parkVehicle(car);
            System.out.println("\nSample Generated Entry Pass:");
            lot.printEntryPass(ticket);
            assertTrue(ticket != null, "Entry pass printed cleanly without runtime errors");
        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }
}
