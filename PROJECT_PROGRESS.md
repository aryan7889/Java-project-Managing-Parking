# Smart Parking & Parking Slot Management System
## Academic Project Tracking & Documentation

---

### Project Metadata
- **Course**: Object-Oriented Programming using Java (OOPS)
- **Application Type**: Console-based Menu-driven System + Optional Visual Web Dashboard
- **Language & Runtime**: Java 8+ (Tested on Java 26 / OpenJDK)
- **Persistence**: Plain text file storage (`java.io` / `java.nio`)
- **Status**: 🟢 ALL 7 PHASES COMPLETED AND VERIFIED (167/167 Tests Passed)

---

### 1. Phase Progress Tracker

| Phase | Description | Status |
|---|---|---|
| **Phase 0** | Requirement Analysis, Architecture & Planning | 🟢 Completed & Approved |
| **Phase 1** | Project Setup & Core Class Design (Models & OOP) | 🟢 Completed & Verified (26/26 Tests Passed) |
| **Phase 2** | Parking Lot & Slot Management | 🟢 Completed & Verified (28/28 Tests Passed) |
| **Phase 3** | Vehicle Entry & Ticket Management | 🟢 Completed & Verified (28/28 Tests Passed) |
| **Phase 4** | Vehicle Exit, Duration & Billing | 🟢 Completed & Verified (30/30 Tests Passed) |
| **Phase 5** | File Handling & Data Persistence | 🟢 Completed & Verified (28/28 Tests Passed) |
| **Phase 6** | Menu Integration, Custom Exceptions & Complete Testing | 🟢 Completed & Verified (27/27 Tests Passed) |
| **Phase 7** | Documentation, UML Diagrams, Viva Prep & Web Visualizer | 🟢 Completed & Verified |

---

### 2. Completed Tasks across Entire Project
- **Core Java OOP Application**: 100% console-driven, adhering strictly to academic constraints (no Spring Boot, no Hibernate, no MySQL, pure standard Java JDK 8+).
- **Interactive Visual Web Dashboard**: Created interactive 2D parking lot visualizer (`web/index.html` and Antigravity artifact `smart_parking_dashboard.html`) for project exhibitions, live presentations, and visual demonstrations.
- **Optional Built-in Java Web Server**: Added `SmartParkingWebServer.java` using standard JDK `com.sun.net.httpserver.HttpServer` (zero external dependencies) to serve the dashboard on `http://localhost:8080`.
- **Academic Documentation**: Produced `PROJECT_REPORT.md` and `VIVA_QUESTIONS.md` (Top 20 questions & answers).
- **Testing**: 167 automated unit and integration tests passing across 6 test suites.

---

### 3. File Manifest
- `PROJECT_PROGRESS.md`: Project status tracking file.
- `PROJECT_REPORT.md`: Comprehensive academic project report and documentation.
- `VIVA_QUESTIONS.md`: 20 frequently asked Java viva questions with high-scoring answers.
- `web/index.html`: Interactive Visual Web Dashboard (Tailwind CSS, 2D parking grid, pass/invoice modals).
- `src/com/smartparking/ParkingManager.java`: Main application controller & console interface.
- `src/com/smartparking/web/SmartParkingWebServer.java`: Built-in JDK HTTP server for the web visualizer.
- `src/com/smartparking/model/VehicleType.java`: Vehicle type enum (`BIKE`, `CAR`, `TRUCK`).
- `src/com/smartparking/model/TicketStatus.java`: Ticket lifecycle enum (`ACTIVE`, `COMPLETED`).
- `src/com/smartparking/model/Vehicle.java`: Abstract parent model class.
- `src/com/smartparking/model/Bike.java`: Concrete Bike class (₹10/hr).
- `src/com/smartparking/model/Car.java`: Concrete Car class (₹20/hr).
- `src/com/smartparking/model/Truck.java`: Concrete Truck class (₹40/hr).
- `src/com/smartparking/model/ParkingSlot.java`: Slot management model.
- `src/com/smartparking/model/ParkingTicket.java`: Ticket management model.
- `src/com/smartparking/exception/ParkingException.java`: Base custom checked exception.
- `src/com/smartparking/exception/SlotUnavailableException.java`: Slot unavailable exception.
- `src/com/smartparking/exception/DuplicateVehicleException.java`: Duplicate entry exception.
- `src/com/smartparking/exception/InvalidTicketException.java`: Ticket lookup exception.
- `src/com/smartparking/util/VehicleFactory.java`: Factory class for vehicle creation and validation.
- `src/com/smartparking/service/ParkingLot.java`: Core service for slot allocation, tickets, checkout, and history.
- `src/com/smartparking/service/FileStorageService.java`: File handling and data persistence service.
- `src/com/smartparking/test/Phase1Test.java`: Phase 1 test suite (26 assertions).
- `src/com/smartparking/test/Phase2Test.java`: Phase 2 test suite (28 assertions).
- `src/com/smartparking/test/Phase3Test.java`: Phase 3 test suite (28 assertions).
- `src/com/smartparking/test/Phase4Test.java`: Phase 4 test suite (30 assertions).
- `src/com/smartparking/test/Phase5Test.java`: Phase 5 test suite (28 assertions).
- `src/com/smartparking/test/Phase6Test.java`: Phase 6 integration test suite (27 assertions).

---

### 4. How to Run
#### Console Application:
```powershell
java -cp bin com.smartparking.ParkingManager
```

#### Visual Web Dashboard (Option A - Double-click or Browser):
Open `web/index.html` in Chrome, Edge, or any web browser.

#### Visual Web Dashboard (Option B - Built-in Java Server):
```powershell
java -cp bin com.smartparking.web.SmartParkingWebServer
# Open http://localhost:8080 in your browser
```
