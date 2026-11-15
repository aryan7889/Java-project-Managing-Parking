package com.smartparking.test;

import com.smartparking.model.Bike;
import com.smartparking.model.Car;
import com.smartparking.model.Truck;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.model.ParkingSlot;
import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;

import java.time.LocalDateTime;

/**
 * Phase 1 Test Program.
 * Verifies core class design, OOP principles (Encapsulation, Inheritance, Abstraction, Polymorphism),
 * and independent model behavior before building complex workflows.
 */
public class Phase1Test {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   SMART PARKING SYSTEM - PHASE 1 TEST SUITE      ");
        System.out.println("==================================================");

        testVehicleInheritanceAndEncapsulation();
        testPolymorphicFeeCalculation();
        testParkingSlotAllocationAndRelease();
        testParkingTicketLifecycle();

        System.out.println("==================================================");
        System.out.println(String.format("TEST RESULTS: %d/%d Passed", testsPassed, totalTests));
        if (testsPassed == totalTests) {
            System.out.println("STATUS: ALL PHASE 1 TESTS PASSED SUCCESSFULLY! ✓");
        } else {
            System.out.println("STATUS: SOME TESTS FAILED. PLEASE REVIEW LOGS. ✗");
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

    private static void testVehicleInheritanceAndEncapsulation() {
        System.out.println("\n--- Test Suite 1: Vehicle Inheritance & Encapsulation ---");

        Vehicle bike = new Bike("MH-12-BK-0001", "Aarav Sharma");
        Vehicle car = new Car("DL-01-CA-9999", "Priya Verma");
        Vehicle truck = new Truck("KA-05-TR-5555", "Rajesh Patel");

        assertTrue(bike instanceof Vehicle, "Bike is an instance of Vehicle (Inheritance)");
        assertTrue(car instanceof Vehicle, "Car is an instance of Vehicle (Inheritance)");
        assertTrue(truck instanceof Vehicle, "Truck is an instance of Vehicle (Inheritance)");

        assertTrue(bike.getVehicleType() == VehicleType.BIKE, "Bike has VehicleType.BIKE");
        assertTrue(car.getVehicleType() == VehicleType.CAR, "Car has VehicleType.CAR");
        assertTrue(truck.getVehicleType() == VehicleType.TRUCK, "Truck has VehicleType.TRUCK");

        assertTrue("MH-12-BK-0001".equals(bike.getRegistrationNumber()), "Bike registration encapsulated & uppercase");
        assertTrue("Priya Verma".equals(car.getOwnerName()), "Car owner name getter works");

        // Test defensive validation
        boolean caughtException = false;
        try {
            new Car("", "Empty Reg Owner");
        } catch (IllegalArgumentException e) {
            caughtException = true;
        }
        assertTrue(caughtException, "Constructor rejects blank registration number");
    }

    private static void testPolymorphicFeeCalculation() {
        System.out.println("\n--- Test Suite 2: Polymorphism (Fee Calculation) ---");

        // Array of abstract Vehicle references holding concrete subclass objects
        Vehicle[] fleet = new Vehicle[] {
                new Bike("B1-REG", "Owner 1"),
                new Car("C1-REG", "Owner 2"),
                new Truck("T1-REG", "Owner 3")
        };

        long testDurationHours = 3;

        // Bike: 3 hrs * ₹10 = ₹30.0
        double bikeFee = fleet[0].calculateFee(testDurationHours);
        assertTrue(bikeFee == 30.0, String.format("Bike 3h polymorphic fee == ₹30.0 (Actual: ₹%.1f)", bikeFee));

        // Car: 3 hrs * ₹20 = ₹60.0
        double carFee = fleet[1].calculateFee(testDurationHours);
        assertTrue(carFee == 60.0, String.format("Car 3h polymorphic fee == ₹60.0 (Actual: ₹%.1f)", carFee));

        // Truck: 3 hrs * ₹40 = ₹120.0
        double truckFee = fleet[2].calculateFee(testDurationHours);
        assertTrue(truckFee == 120.0, String.format("Truck 3h polymorphic fee == ₹120.0 (Actual: ₹%.1f)", truckFee));

        // Minimum duration rule (0 or negative hours defaults to 1 hour minimum)
        double minFee = fleet[1].calculateFee(0);
        assertTrue(minFee == 20.0, "Car with 0 hours pays 1 hour minimum fee (₹20.0)");
    }

    private static void testParkingSlotAllocationAndRelease() {
        System.out.println("\n--- Test Suite 3: ParkingSlot Allocation & Guard Checks ---");

        ParkingSlot carSlot = new ParkingSlot("C-101", VehicleType.CAR);
        Vehicle car = new Car("MH-14-AA-1111", "Rohan");
        Vehicle bike = new Bike("MH-14-BB-2222", "Karan");

        assertTrue(!carSlot.isOccupied(), "New slot is initially vacant");

        // Compatible assignment
        boolean assignedCar = carSlot.assignVehicle(car);
        assertTrue(assignedCar && carSlot.isOccupied(), "Compatible Car assigned to Car slot successfully");
        assertTrue(carSlot.getAssignedVehicle().equals(car), "Slot assigned vehicle matches parked car");

        // Attempt duplicate assignment on occupied slot
        boolean doubleBooked = carSlot.assignVehicle(car);
        assertTrue(!doubleBooked, "Cannot assign another vehicle to an occupied slot");

        // Release slot
        carSlot.releaseSlot();
        assertTrue(!carSlot.isOccupied() && carSlot.getAssignedVehicle() == null, "Slot released and reset to vacant");

        // Type mismatch check: Try assigning Bike to Car slot
        boolean typeMismatch = carSlot.assignVehicle(bike);
        assertTrue(!typeMismatch && !carSlot.isOccupied(), "Slot rejects incompatible vehicle type (Bike in Car slot)");
    }

    private static void testParkingTicketLifecycle() {
        System.out.println("\n--- Test Suite 4: ParkingTicket Lifecycle ---");

        Vehicle car = new Car("KA-01-MG-1234", "Sunil");
        LocalDateTime entryTime = LocalDateTime.now().minusHours(2).minusMinutes(15);
        ParkingTicket ticket = new ParkingTicket("TCK-1001", car, "C-01", entryTime);

        assertTrue("TCK-1001".equals(ticket.getTicketId()), "Ticket ID properly initialized");
        assertTrue(ticket.getStatus() == TicketStatus.ACTIVE, "New ticket starts with ACTIVE status");
        assertTrue(ticket.getVehicle().equals(car), "Ticket holds correct vehicle reference");

        // Simulate checkout after 3 hours (ceiling of 2h 15m)
        LocalDateTime exitTime = LocalDateTime.now();
        long durationHours = 3;
        double fee = car.calculateFee(durationHours); // ₹60.0

        ticket.completeTicket(exitTime, durationHours, fee);

        assertTrue(ticket.getStatus() == TicketStatus.COMPLETED, "Completed ticket has COMPLETED status");
        assertTrue(ticket.getDurationHours() == 3, "Ticket duration recorded as 3 hours");
        assertTrue(ticket.getFeeCharged() == 60.0, "Ticket fee charged recorded as ₹60.0");
        assertTrue(ticket.getExitTime() != null, "Ticket exit timestamp recorded");
    }
}
