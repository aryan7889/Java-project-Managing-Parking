package com.smartparking.test;

import com.smartparking.exception.DuplicateVehicleException;
import com.smartparking.exception.SlotUnavailableException;
import com.smartparking.model.Bike;
import com.smartparking.model.Car;
import com.smartparking.model.Truck;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.service.ParkingLot;

import java.util.List;

/**
 * Phase 2 Test Program.
 * Verifies ParkingLot slot initialization, automatic slot lookup, allocation,
 * release, duplicate vehicle prevention, and slot-full handling.
 */
public class Phase2Test {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 2 TEST SUITE      ");
        System.out.println("==================================================");

        testSlotInitialization();
        testAllocationForAllVehicleTypes();
        testDuplicateVehicleAllocationRejection();
        testParkingLotFullHandling();
        testSlotReleaseAndReallocation();
        testSlotFilteringAndDisplay();

        System.out.println("==================================================");
        System.out.printf("PHASE 2 TEST RESULTS: %d/%d Passed%n", testsPassed, totalTests);
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL PHASE 2 TESTS PASSED SUCCESSFULLY! ✓");
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

    private static void testSlotInitialization() {
        System.out.println("\n--- Test Suite 1: Slot Initialization ---");

        // Custom capacity: 2 Bikes, 3 Cars, 1 Truck = 6 total slots
        ParkingLot lot = new ParkingLot(2, 3, 1);

        assertTrue(lot.getTotalSlotsCount() == 6, "Total slots initialized matches 2 + 3 + 1 = 6");
        assertTrue(lot.getAvailableSlotsCount() == 6, "All 6 slots are initially vacant");
        assertTrue(lot.getOccupiedSlotsCount() == 0, "0 slots occupied initially");

        assertTrue(lot.getSlotsByType(VehicleType.BIKE).size() == 2, "Configured 2 Bike slots");
        assertTrue(lot.getSlotsByType(VehicleType.CAR).size() == 3, "Configured 3 Car slots");
        assertTrue(lot.getSlotsByType(VehicleType.TRUCK).size() == 1, "Configured 1 Truck slot");

        assertTrue("B-1".equals(lot.getSlotsByType(VehicleType.BIKE).get(0).getSlotId()), "First bike slot is B-1");
        assertTrue("C-1".equals(lot.getSlotsByType(VehicleType.CAR).get(0).getSlotId()), "First car slot is C-1");
        assertTrue("T-1".equals(lot.getSlotsByType(VehicleType.TRUCK).get(0).getSlotId()), "First truck slot is T-1");
    }

    private static void testAllocationForAllVehicleTypes() {
        System.out.println("\n--- Test Suite 2: Slot Allocation for All Vehicle Types ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);

        Vehicle bike = new Bike("MH-01-BK-1111", "Sameer");
        Vehicle car = new Car("MH-02-CR-2222", "Ananya");
        Vehicle truck = new Truck("MH-03-TR-3333", "Balram");

        try {
            ParkingSlot bikeSlot = lot.allocateSlot(bike);
            assertTrue("B-1".equals(bikeSlot.getSlotId()), "Bike allocated to first vacant bike slot B-1");
            assertTrue(bikeSlot.isOccupied(), "B-1 is now marked occupied");

            ParkingSlot carSlot = lot.allocateSlot(car);
            assertTrue("C-1".equals(carSlot.getSlotId()), "Car allocated to first vacant car slot C-1");
            assertTrue(carSlot.isOccupied(), "C-1 is now marked occupied");

            ParkingSlot truckSlot = lot.allocateSlot(truck);
            assertTrue("T-1".equals(truckSlot.getSlotId()), "Truck allocated to first vacant truck slot T-1");
            assertTrue(truckSlot.isOccupied(), "T-1 is now marked occupied");

            assertTrue(lot.getOccupiedSlotsCount() == 3, "3 slots occupied in total");
            assertTrue(lot.getAvailableSlotsCount() == 3, "3 slots remain vacant");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception during allocation: " + e.getMessage());
        }
    }

    private static void testDuplicateVehicleAllocationRejection() {
        System.out.println("\n--- Test Suite 3: Duplicate Vehicle Prevention ---");

        ParkingLot lot = new ParkingLot(2, 2, 2);
        Vehicle car = new Car("KA-01-AB-1234", "Vikram");

        try {
            lot.allocateSlot(car); // First allocation succeeds
        } catch (Exception e) {
            assertTrue(false, "First allocation should succeed");
        }

        boolean caughtDuplicate = false;
        try {
            // Attempt to allocate the same vehicle again
            lot.allocateSlot(car);
        } catch (DuplicateVehicleException e) {
            caughtDuplicate = true;
            System.out.println("  [Expected Exception Caught]: " + e.getMessage());
        } catch (Exception e) {
            caughtDuplicate = false;
        }

        assertTrue(caughtDuplicate, "DuplicateVehicleException thrown when vehicle is already parked");
    }

    private static void testParkingLotFullHandling() {
        System.out.println("\n--- Test Suite 4: Lot-Full Handling (SlotUnavailableException) ---");

        // Small capacity lot: 1 Bike, 1 Car, 1 Truck
        ParkingLot lot = new ParkingLot(1, 1, 1);

        Vehicle car1 = new Car("UP-16-C1", "Owner A");
        Vehicle car2 = new Car("UP-16-C2", "Owner B");

        try {
            lot.allocateSlot(car1); // Takes C-1
        } catch (Exception e) {
            assertTrue(false, "Car 1 allocation should succeed");
        }

        boolean caughtFullException = false;
        try {
            lot.allocateSlot(car2); // No more car slots
        } catch (SlotUnavailableException e) {
            caughtFullException = true;
            System.out.println("  [Expected Exception Caught]: " + e.getMessage());
        } catch (Exception e) {
            caughtFullException = false;
        }

        assertTrue(caughtFullException, "SlotUnavailableException thrown when car slots are full");
    }

    private static void testSlotReleaseAndReallocation() {
        System.out.println("\n--- Test Suite 5: Slot Release & Reallocation ---");

        ParkingLot lot = new ParkingLot(1, 1, 1);
        Vehicle car1 = new Car("DL-01-A-1", "Owner 1");
        Vehicle car2 = new Car("DL-01-A-2", "Owner 2");

        try {
            ParkingSlot allocatedSlot = lot.allocateSlot(car1);
            assertTrue("C-1".equals(allocatedSlot.getSlotId()), "Car 1 initially takes slot C-1");

            // Release slot by slot ID
            boolean released = lot.releaseSlot("C-1");
            assertTrue(released, "Slot C-1 released successfully");
            assertTrue(!allocatedSlot.isOccupied(), "Slot C-1 is now vacant");
            assertTrue(lot.getAvailableSlotsCount() == 3, "All 3 slots available again");

            // Now car2 can take the freshly released slot
            ParkingSlot reallocated = lot.allocateSlot(car2);
            assertTrue("C-1".equals(reallocated.getSlotId()), "Slot C-1 successfully reallocated to Car 2");

            // Release by vehicle registration number
            boolean releasedByReg = lot.releaseSlotForVehicle("DL-01-A-2");
            assertTrue(releasedByReg, "Slot C-1 released by vehicle registration number");
            assertTrue(!lot.isVehicleParked("DL-01-A-2"), "Vehicle DL-01-A-2 confirmed no longer parked");

        } catch (Exception e) {
            assertTrue(false, "Unexpected exception: " + e.getMessage());
        }
    }

    private static void testSlotFilteringAndDisplay() {
        System.out.println("\n--- Test Suite 6: Slot Filtering & Table Output Verification ---");

        ParkingLot lot = new ParkingLot(2, 2, 1);
        Vehicle bike = new Bike("TN-09-BK-99", "Manoj");

        try {
            lot.allocateSlot(bike);
        } catch (Exception e) {
            // Ignored
        }

        List<ParkingSlot> available = lot.getAvailableSlots();
        List<ParkingSlot> occupied = lot.getOccupiedSlots();

        assertTrue(occupied.size() == 1, "Exactly 1 occupied slot found");
        assertTrue(available.size() == 4, "Exactly 4 available slots found");

        System.out.println("\nSample Table Display Demonstration:");
        lot.displayAllSlots();
        lot.displayOccupiedSlots();
    }
}
