package com.smartparking.test;

import com.smartparking.exception.InvalidTicketException;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.service.ParkingLot;
import com.smartparking.util.VehicleFactory;

import java.time.LocalDateTime;

/**
 * Phase 4 Test Program.
 * Verifies Vehicle Exit workflow, duration calculations, ceiling rounding rules,
 * polymorphic fee computation, slot release, ticket lifecycle completion,
 * and invoice generation.
 */
public class Phase4Test {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 4 TEST SUITE      ");
        System.out.println("==================================================");

        testCheckoutByTicketId();
        testCheckoutByVehicleRegistrationNumber();
        testCeilingDurationAndPolymorphicBilling();
        testSlotReleaseAndImmediateReallocation();
        testTicketCompletedStateAndHistoryArchiving();
        testInvalidCheckoutScenarios();
        testInvoiceAndHistoryDisplay();

        System.out.println("==================================================");
        System.out.printf("PHASE 4 TEST RESULTS: %d/%d Passed%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL PHASE 4 TESTS PASSED SUCCESSFULLY! ✓");
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

    private static void testCheckoutByTicketId() {
        System.out.println("\n--- Test Suite 1: Checkout By Ticket ID ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = VehicleFactory.createVehicle("MH-12-CD-5678", "Nitin Gadkari", VehicleType.CAR);

        try {
            ParkingTicket entryTicket = lot.parkVehicle(car);
            String ticketId = entryTicket.getTicketId();

            assertTrue(lot.isVehicleParked("MH-12-CD-5678"), "Vehicle initially marked as parked");

            // Checkout after 2 hours
            LocalDateTime exitTime = entryTicket.getEntryTime().plusHours(2);
            ParkingTicket exitTicket = lot.checkoutVehicle(ticketId, exitTime);

            assertTrue(exitTicket != null, "Exit ticket returned successfully");
            assertTrue(exitTicket.getStatus() == TicketStatus.COMPLETED, "Ticket marked as COMPLETED");
            assertTrue(exitTicket.getDurationHours() == 2, "Billed duration is 2 hours");
            assertTrue(exitTicket.getFeeCharged() == 40.0, "Car 2 hours @ ₹20 = ₹40.0");
            assertTrue(!lot.isVehicleParked("MH-12-CD-5678"), "Vehicle is no longer actively parked");
            assertTrue(lot.getActiveTicketCount() == 0, "Active ticket count decremented to 0");
            assertTrue(lot.getCompletedSessionsCount() == 1, "Completed history count incremented to 1");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testCheckoutByVehicleRegistrationNumber() {
        System.out.println("\n--- Test Suite 2: Checkout By Vehicle Registration Number ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle bike = VehicleFactory.createVehicle("DL-03-BK-9999", "Rohit", VehicleType.BIKE);

        try {
            ParkingTicket entryTicket = lot.parkVehicle(bike);

            // Checkout using registration number with 1 hour duration
            LocalDateTime exitTime = entryTicket.getEntryTime().plusHours(1);
            ParkingTicket exitTicket = lot.checkoutVehicle("dl-03-bk-9999", exitTime);

            assertTrue("DL-03-BK-9999".equals(exitTicket.getVehicle().getRegistrationNumber()),
                    "Checkout succeeds using case-insensitive vehicle number");
            assertTrue(exitTicket.getFeeCharged() == 10.0, "Bike 1 hour @ ₹10 = ₹10.0");
            assertTrue(exitTicket.getStatus() == TicketStatus.COMPLETED, "Ticket status is COMPLETED");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testCeilingDurationAndPolymorphicBilling() {
        System.out.println("\n--- Test Suite 3: Duration Ceiling Rule & Polymorphic Billing ---");

        ParkingLot lot = new ParkingLot(3, 3, 3);
        LocalDateTime baseEntry = LocalDateTime.of(2026, 11, 14, 10, 0, 0);

        try {
            // Scenario A: 25 minutes (less than 1 hr) -> 1 hour minimum
            Vehicle bike = VehicleFactory.createVehicle("MH-01-BK-25M", "Owner A", VehicleType.BIKE);
            ParkingTicket tBike = lot.parkVehicle(bike);
            ParkingTicket rBike = lot.checkoutVehicle(tBike.getTicketId(), tBike.getEntryTime().plusMinutes(25));
            assertTrue(rBike.getDurationHours() == 1, "25 minutes rounds up to 1 hour (Ceiling Rule)");
            assertTrue(rBike.getFeeCharged() == 10.0, "Bike 1 hr = ₹10.0 (Polymorphic rate ₹10/hr)");

            // Scenario B: 1 hour 10 minutes (70 mins) -> 2 hours
            Vehicle car = VehicleFactory.createVehicle("MH-01-CR-70M", "Owner B", VehicleType.CAR);
            ParkingTicket tCar = lot.parkVehicle(car);
            ParkingTicket rCar = lot.checkoutVehicle(tCar.getTicketId(), tCar.getEntryTime().plusMinutes(70));
            assertTrue(rCar.getDurationHours() == 2, "1 hr 10 mins (70m) rounds up to 2 hours (Ceiling Rule)");
            assertTrue(rCar.getFeeCharged() == 40.0, "Car 2 hrs = ₹40.0 (Polymorphic rate ₹20/hr)");

            // Scenario C: 3 hours 1 minute (181 mins) -> 4 hours
            Vehicle truck = VehicleFactory.createVehicle("MH-01-TR-181M", "Owner C", VehicleType.TRUCK);
            ParkingTicket tTruck = lot.parkVehicle(truck);
            ParkingTicket rTruck = lot.checkoutVehicle(tTruck.getTicketId(), tTruck.getEntryTime().plusMinutes(181));
            assertTrue(rTruck.getDurationHours() == 4, "3 hr 1 min (181m) rounds up to 4 hours (Ceiling Rule)");
            assertTrue(rTruck.getFeeCharged() == 160.0, "Truck 4 hrs = ₹160.0 (Polymorphic rate ₹40/hr)");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception during ceiling tests: " + e.getMessage());
        }
    }

    private static void testSlotReleaseAndImmediateReallocation() {
        System.out.println("\n--- Test Suite 4: Slot Release & Immediate Reallocation ---");

        // Single car slot lot
        ParkingLot lot = new ParkingLot(1, 1, 1);
        Vehicle car1 = VehicleFactory.createVehicle("KA-01-C1", "First Driver", VehicleType.CAR);
        Vehicle car2 = VehicleFactory.createVehicle("KA-01-C2", "Second Driver", VehicleType.CAR);

        try {
            ParkingTicket t1 = lot.parkVehicle(car1);
            assertTrue("C-1".equals(t1.getSlotId()), "Car 1 allocated slot C-1");
            assertTrue(lot.getAvailableSlotsCount() == 2, "2 slots available (Bike & Truck)");

            // Checkout Car 1
            lot.checkoutVehicle(t1.getTicketId(), t1.getEntryTime().plusHours(1));

            // Slot C-1 should now be vacant
            assertTrue(!lot.findSlotById("C-1").isOccupied(), "Slot C-1 is immediately vacant after checkout");
            assertTrue(lot.getAvailableSlotsCount() == 3, "All 3 slots available again");

            // Car 2 can now successfully enter and take slot C-1
            ParkingTicket t2 = lot.parkVehicle(car2);
            assertTrue("C-1".equals(t2.getSlotId()), "Slot C-1 successfully reallocated to Car 2");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testTicketCompletedStateAndHistoryArchiving() {
        System.out.println("\n--- Test Suite 5: Ticket Completed State & History Archiving ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle bike = VehicleFactory.createVehicle("RJ-01-BK-11", "Ramesh", VehicleType.BIKE);
        Vehicle car = VehicleFactory.createVehicle("RJ-01-CR-22", "Suresh", VehicleType.CAR);

        try {
            ParkingTicket t1 = lot.parkVehicle(bike);
            ParkingTicket t2 = lot.parkVehicle(car);

            lot.checkoutVehicle(t1.getTicketId(), t1.getEntryTime().plusHours(1)); // ₹10
            lot.checkoutVehicle(t2.getTicketId(), t2.getEntryTime().plusHours(2)); // ₹40

            assertTrue(lot.getActiveTicketCount() == 0, "No active tickets remaining");
            assertTrue(lot.getCompletedSessionsCount() == 2, "2 sessions archived in history");
            assertTrue(lot.getTotalRevenue() == 50.0, "Total revenue is ₹10 + ₹40 = ₹50.0");

            // Check history search
            ParkingTicket hist1 = lot.findTicketById(t1.getTicketId());
            assertTrue(hist1 != null && hist1.getStatus() == TicketStatus.COMPLETED,
                    "findTicketById retrieves completed ticket from history");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testInvalidCheckoutScenarios() {
        System.out.println("\n--- Test Suite 6: Invalid Checkout Handling ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);

        // Attempt checkout on non-existent ticket ID
        boolean caughtNonExistent = false;
        try {
            lot.checkoutVehicle("TCK-9999");
        } catch (InvalidTicketException e) {
            caughtNonExistent = true;
            System.out.println("  [Expected Exception Caught]: " + e.getMessage());
        } catch (Exception e) {
            caughtNonExistent = false;
        }
        assertTrue(caughtNonExistent, "InvalidTicketException thrown for non-existent ticket ID");

        // Attempt checkout twice on the same ticket
        try {
            Vehicle car = VehicleFactory.createVehicle("GA-01-CR-55", "Goa Driver", VehicleType.CAR);
            ParkingTicket ticket = lot.parkVehicle(car);
            lot.checkoutVehicle(ticket.getTicketId()); // First checkout succeeds

            boolean caughtDoubleCheckout = false;
            try {
                lot.checkoutVehicle(ticket.getTicketId()); // Second checkout fails
            } catch (InvalidTicketException e) {
                caughtDoubleCheckout = true;
                System.out.println("  [Expected Exception Caught]: " + e.getMessage());
            }
            assertTrue(caughtDoubleCheckout, "Cannot checkout an already completed ticket");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testInvoiceAndHistoryDisplay() {
        System.out.println("\n--- Test Suite 7: Invoice & History Rendering Verification ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle truck = VehicleFactory.createVehicle("HP-01-TR-9999", "Himachal Haulers", VehicleType.TRUCK);

        try {
            ParkingTicket ticket = lot.parkVehicle(truck);
            LocalDateTime exitTime = ticket.getEntryTime().plusHours(3).plusMinutes(15);
            ParkingTicket completed = lot.checkoutVehicle(ticket.getTicketId(), exitTime);

            System.out.println("\nSample Exit Invoice Display:");
            lot.printCheckoutReceipt(completed);

            System.out.println("\nSample Parking History Table:");
            lot.displayParkingHistory();

            assertTrue(completed.getDurationHours() == 4, "3h 15m billed for 4 hours");
            assertTrue(completed.getFeeCharged() == 160.0, "Truck fee ₹160.0");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }
}
