# Smart Parking Management System — Java Viva Preparation Guide
## Top 20 Frequently Asked Viva Questions & Model Answers

---

### Category 1: Object-Oriented Programming (OOP) Concepts

#### Q1. Explain how the four pillars of OOP are used in this project.
- **Encapsulation**: All fields in `Vehicle`, `ParkingSlot`, and `ParkingTicket` are `private`. They are accessed and mutated strictly through validated getters and setters.
- **Inheritance**: `Bike`, `Car`, and `Truck` extend the abstract `Vehicle` class, inheriting common attributes (`registrationNumber`, `ownerName`, `vehicleType`).
- **Abstraction**: `Vehicle` is declared `abstract`. It declares the abstract method `calculateFee(long durationHours)`. It cannot be instantiated directly, hiding specific pricing formulas from the rest of the application.
- **Polymorphism**: In `ParkingLot`, during checkout, we call `ticket.getVehicle().calculateFee(hours)`. The JVM dynamically resolves and executes the subclass's overridden fee method (`Bike` ₹10/hr, `Car` ₹20/hr, `Truck` ₹40/hr).

---

#### Q2. What is the difference between Method Overloading and Method Overriding in your project?
- **Method Overriding**: `calculateFee(long durationHours)` in `Vehicle` is overridden by `Bike`, `Car`, and `Truck` with specific rate formulas. It occurs across superclass-subclass boundaries with identical signatures.
- **Method Overloading**: In `ParkingLot`, we overloaded `checkoutVehicle(String identifier)` and `checkoutVehicle(String identifier, LocalDateTime exitTime)`. Both methods share the same name within the same class but differ in parameter count, allowing deterministic testing of exit times.

---

#### Q3. Why did you use an Abstract Class instead of an Interface for `Vehicle`?
- An abstract class is suitable when subclasses share common non-static state (`registrationNumber`, `ownerName`) and code (`getRegistrationNumber()`, `toString()`).
- An interface in Java cannot store instance state (variables are implicitly `public static final`). Since all vehicles have state and an `is-a` relationship, an abstract class is the architecturally correct choice.

---

#### Q4. What is Dynamic Method Dispatch?
- Dynamic method dispatch is the mechanism by which a call to an overridden method is resolved at runtime rather than compile time.
- In our project:
  ```java
  Vehicle v = ticket.getVehicle();
  double fee = v.calculateFee(hours);
  ```
  The compiler only checks that `Vehicle` declares `calculateFee()`. At runtime, the JVM inspects the actual object type (`Bike`, `Car`, or `Truck`) and invokes the corresponding implementation.

---

### Category 2: Exception Handling

#### Q5. What is the difference between Checked and Unchecked exceptions? Which ones did you use?
- **Checked Exceptions**: Extend `java.lang.Exception`. The compiler forces the programmer to handle them using `try-catch` or declare them using `throws`.
- **Unchecked Exceptions**: Extend `java.lang.RuntimeException`. Compiler does not force handling (e.g. `NullPointerException`, `IllegalArgumentException`).
- In our project, custom exceptions (`SlotUnavailableException`, `DuplicateVehicleException`, `InvalidTicketException`) extend `ParkingException`, which extends `Exception` (checked). This ensures business rule violations must be handled explicitly by the UI.

---

#### Q6. What happens if a user enters letters when a number is expected in the menu?
- In `ParkingManager.readMenuChoice()`, we read the input as a String and parse it with `Integer.parseInt(input)` inside a `try-catch (NumberFormatException e)`.
- If a user inputs `"abc"`, it catches the exception and prompts the user to enter a valid number without crashing the application.

---

#### Q7. What is the try-with-resources statement, and where is it used?
- Introduced in Java 7, `try-with-resources` automatically closes any resource implementing `java.lang.AutoCloseable` at the end of the block.
- We used it in `FileStorageService`:
  ```java
  try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      // Stream is automatically closed even if an exception occurs
  }
  ```
  This eliminates memory leaks and dangling file locks.

---

### Category 3: System Design & Business Logic

#### Q8. How is the Ceiling Rule for parking fee calculated?
- Any fraction of an hour is charged as a full hour.
- We calculate duration in minutes using `Duration.between(entryTime, exitTime).toMinutes()`.
- The ceiling rule is implemented as:
  ```java
  long billableHours = (minutes <= 0) ? 1 : (long) Math.ceil(minutes / 60.0);
  ```
- For example, 1 hour 10 minutes (70 mins) gives `70 / 60.0 = 1.1667`, and `Math.ceil(1.1667)` evaluates to `2` hours. Minimum duration is 1 hour.

---

#### Q9. How do you prevent duplicate vehicle parking?
- When `ParkingLot.allocateSlot(Vehicle vehicle)` is called, it checks `isVehicleParked(registrationNumber)`.
- If the vehicle is already parked in any slot or active ticket, it throws `DuplicateVehicleException`.

---

#### Q10. How is slot allocation handled when a vehicle enters?
- `ParkingLot.findAvailableSlot(VehicleType type)` iterates through the slot list and returns the first slot where `supportedType == type` and `!occupied`.
- Dedicated prefixes are used: `B-*` for Bikes, `C-*` for Cars, and `T-*` for Trucks.

---

#### Q11. How do you ensure data consistency between active tickets and slot occupancy on application restart?
- On startup, `ParkingLot` initializes empty slots.
- When `FileStorageService.loadActiveTickets()` reads `active_tickets.txt`, it passes each ticket to `parkingLot.restoreActiveTicket(ticket)`.
- This assigns the vehicle to its specific slot and marks the slot as `OCCUPIED`.
- This ensures 100% synchronization without maintaining separate, potentially contradictory slot files.

---

#### Q12. Why did you use `VehicleFactory`?
- It implements the Factory design pattern.
- It encapsulates the decision of which subclass (`Bike`, `Car`, `Truck`) to instantiate based on `VehicleType`.
- It validates registration numbers, trims whitespaces, and converts plates to uppercase before object instantiation.

---

### Category 4: Java Collections & I/O

#### Q13. Which Java Collections did you use and why?
- `List<ParkingSlot>` (`ArrayList`): Maintains ordered parking slots.
- `Map<String, ParkingTicket>` (`LinkedHashMap`): Fast $O(1)$ lookup for active tickets by ticket ID while preserving insertion order.
- `List<ParkingTicket>` (`ArrayList`): Chronological log of completed sessions in parking history.

#### Q14. Why do getters like `getAllSlots()` return `Collections.unmodifiableList()`?
- To enforce encapsulation and prevent external code from tampering with the internal collections (defensive programming).

#### Q15. Why use pipe delimiter (`|`) instead of CSV comma?
- Vehicle owner names can contain commas (e.g. "Rathore, Vikram"). Using commas could split a single field into two. Pipe delimiter (`|`) guarantees collision-free parsing.

---

### Quick Revision Summary

| Concept | File / Location | Purpose |
|---|---|---|
| Abstract Class | `Vehicle.java` | Base template for all vehicle types |
| Dynamic Dispatch | `ParkingLot.checkoutVehicle()` | Polymorphic fee calculation |
| Checked Exception | `SlotUnavailableException.java` | Prevents parking when full |
| Factory Pattern | `VehicleFactory.java` | Clean object construction |
| File I/O | `FileStorageService.java` | Persistent text file storage |
| Menu Loop | `ParkingManager.java` | Interactive user interface |
