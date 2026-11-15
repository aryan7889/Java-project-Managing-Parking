# 🚗 Smart Parking & Parking Slot Management System

[![Java](https://img.shields.io/badge/Java-8%2B%20%7C%2011%20%7C%2017%20%7C%2021-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-167%2F167%20Passing%20(100%25)-success)](src/com/smartparking/test/)
[![Dependencies](https://img.shields.io/badge/Dependencies-Zero%20(Pure%20JDK)-brightgreen)](#prerequisites)
[![Architecture](https://img.shields.io/badge/Architecture-Object--Oriented%20(OOP)-orange)](#-core-oop-concepts-demonstrated)

A robust, console-driven **Smart Parking & Parking Slot Management System** engineered in pure Java. Designed as an academic capstone project for **Object-Oriented Programming (OOPS) using Java**, it features automated slot allocation, dynamic polymorphic billing, partial-hour duration ceiling logic, custom checked exceptions, crash-resilient file persistence (`java.io`), and an optional visual web dashboard.

---

## 📌 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Core OOP Concepts Demonstrated](#-core-oop-concepts-demonstrated)
- [Pricing & Duration Rules](#-pricing--duration-rules)
- [Project Directory Structure](#-project-directory-structure)
- [Prerequisites](#-prerequisites)
- [Running on a New Device (Step-by-Step)](#-running-on-a-new-device-step-by-step)
  - [Windows Setup](#1-windows-cmd--powershell)
  - [macOS / Linux Setup](#2-macos--linux)
- [Running the Automated Test Suites](#-running-the-automated-test-suites-167-tests)
- [Optional: Interactive Visual Web Dashboard](#-optional-interactive-visual-web-dashboard)
- [Academic Documentation & Viva Material](#-academic-documentation--viva-material)
- [License](#-license)

---

## 📖 Project Overview

Managing urban parking facilities requires fair slot allocation, exact duration tracking, clear vehicle pricing, and persistent record-keeping. This system eliminates manual paperwork through:
1. **Categorized Physical Slots**: Dedicated zones for **Bikes** (`B-1..B-5`), **Cars** (`C-1..C-10`), and **Trucks** (`T-1..T-3`).
2. **Automated Allocation**: Instantly matches arriving vehicles to the nearest vacant, compatible slot.
3. **Sequential Ticket Issuance**: Stamps arrival timestamps (`java.time.LocalDateTime`) and outputs formatted entry passes.
4. **Duration Ceiling Billing**: Partial hours are rounded up to the next full hour; fees are resolved at runtime via dynamic method dispatch.
5. **Crash-Resilient Persistence**: Auto-saves active tickets and completed history to disk (`data/*.txt`) via pure Java I/O with restart slot synchronization.

---

## ✨ Key Features

- **Menu-Driven Console Interface**: 10-option interactive menu with defensive input parsing (prevents scanner crashes on non-numeric or blank input).
- **Zero External Dependencies**: Uses only standard Java Standard Edition (JDK 8+). No external databases, no Spring Boot, no Hibernate.
- **Dynamic Slot Reallocation**: Slots are immediately released upon checkout and made available for newly arriving vehicles.
- **Multi-Parameter Search**: Search records by License Plate Number or Ticket ID across both active and historical sessions.
- **Facility Analytics**: Real-time breakdown of slot occupancy, available capacity, completed sessions, and total revenue collected.
- **Defensive File Parsing**: Gracefully ignores blank lines, comments, and corrupted lines in data files without application termination.

---

## 🏗 System Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                              │
│  ParkingManager: Interactive Console Menu, User Prompts, Table Views   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                         BUSINESS LOGIC LAYER                           │
│  ParkingLot: Slot Management, Allocation, Checkout, Billing, Analytics │
│  VehicleFactory: Defensive Input Validation, Normalized Instantiation  │
└───────────────────────┬───────────────────────────────┬────────────────┘
                        │                               │
┌───────────────────────▼──────────────┐ ┌──────────────▼────────────────┐
│             DOMAIN MODELS            │ │      PERSISTENCE LAYER        │
│  Vehicle (Abstract), Bike, Car, Truck│ │  FileStorageService:          │
│  ParkingSlot, ParkingTicket          │ │  BufferedReader/BufferedWriter│
│  VehicleType (Enum), TicketStatus    │ │  active_tickets.txt, history  │
└──────────────────────────────────────┘ └───────────────────────────────┘
```

---

## 🧬 Core OOP Concepts Demonstrated

| Concept | Concrete Project Implementation |
|---|---|
| **Encapsulation** | All fields across `Vehicle`, `ParkingSlot`, and `ParkingTicket` are declared `private`. Access is regulated via getters/setters with defensive validation. Collections are returned via `Collections.unmodifiableList()`. |
| **Inheritance** | `Bike`, `Car`, and `Truck` extend the abstract class `Vehicle`, inheriting common fields (`registrationNumber`, `ownerName`, `vehicleType`). |
| **Abstraction** | `Vehicle` defines the abstract contract `public abstract double calculateFee(long durationHours);`, hiding vehicle-specific rate formulas from caller classes. |
| **Polymorphism** | Dynamic method dispatch: calling `ticket.getVehicle().calculateFee(hours)` evaluates fees at runtime based on the actual concrete subclass (`Bike` ₹10/hr, `Car` ₹20/hr, `Truck` ₹40/hr) without `if-else` branching. |
| **Factory Pattern** | `VehicleFactory.createVehicle(...)` validates and normalizes license plates (uppercase, trimmed) and instantiates the correct subclass. |
| **Custom Exceptions** | Checked exception hierarchy (`ParkingException`, `SlotUnavailableException`, `DuplicateVehicleException`, `InvalidTicketException`) ensures compile-time error handling. |
| **Java 8 Date & Time** | Uses `java.time.LocalDateTime` and `java.time.Duration` for timezone-safe, immutable time math. |
| **Core File I/O** | `FileStorageService` uses `BufferedReader`, `BufferedWriter`, and `FileWriter` with `try-with-resources` for leak-free persistence. |

---

## 💰 Pricing & Duration Rules

### Hourly Rates
| Vehicle Category | Hourly Rate | Dedicated Slot Prefix |
|---|---|---|
| **Bike** | ₹10.00 / hr | `B-1`, `B-2`, `B-3`, `B-4`, `B-5` |
| **Car** | ₹20.00 / hr | `C-1` through `C-10` |
| **Truck** | ₹40.00 / hr | `T-1`, `T-2`, `T-3` |

### Ceiling Rounding Rule
- Every partial hour counts as a complete hour.
- **Formula**: `billableHours = (minutes <= 0) ? 1 : (long) Math.ceil(minutes / 60.0);`
- **Examples**:
  - Parked for **25 minutes** $\rightarrow$ Billed for **1 hour** (minimum charge).
  - Parked for **60 minutes** $\rightarrow$ Billed for **1 hour**.
  - Parked for **1 hour 10 minutes (70 mins)** $\rightarrow$ Billed for **2 hours**.
  - Parked for **3 hours 1 minute (181 mins)** $\rightarrow$ Billed for **4 hours**.

---

## 📁 Project Directory Structure

```text
javaParking/
├── .gitignore                     # Git ignore rules for bytecode & temp files
├── LICENSE                        # Open-source MIT License
├── README.md                      # Comprehensive project guide
├── PROJECT_PROGRESS.md            # Phase-by-phase development log
├── PROJECT_REPORT.md              # Academic documentation & viva report
├── VIVA_QUESTIONS.md              # Top 20 Java viva questions & model answers
├── run.bat                        # One-click Windows CMD launcher
├── run.ps1                        # One-click Windows PowerShell launcher
├── run.sh                         # One-click Linux / macOS launcher
├── test.bat                       # One-click Windows test runner (167 tests)
├── test.sh                        # One-click Linux / macOS test runner (167 tests)
├── data/                          # Persistent text file storage directory
│   └── .gitkeep                   # Tracks data/ folder in Git
├── web/                           # Visual Web Dashboard assets
│   └── index.html                 # Interactive 2D parking lot visualizer
└── src/
    └── com/
        └── smartparking/
            ├── ParkingManager.java          # Main Application Entry Point & Menu
            ├── model/
            │   ├── Vehicle.java             # Abstract Parent Class
            │   ├── Bike.java                # Bike concrete class (₹10/hr)
            │   ├── Car.java                 # Car concrete class (₹20/hr)
            │   ├── Truck.java               # Truck concrete class (₹40/hr)
            │   ├── ParkingSlot.java         # Slot model with occupancy states
            │   ├── ParkingTicket.java       # Ticket session record
            │   ├── VehicleType.java         # Enum: BIKE, CAR, TRUCK
            │   └── TicketStatus.java        # Enum: ACTIVE, COMPLETED
            ├── exception/
            │   ├── ParkingException.java           # Base checked exception
            │   ├── SlotUnavailableException.java   # Thrown when lot is full
            │   ├── DuplicateVehicleException.java  # Duplicate entry guard
            │   └── InvalidTicketException.java     # Lookup failure guard
            ├── service/
            │   ├── ParkingLot.java          # Core slot allocation & billing engine
            │   └── FileStorageService.java  # Pipe-delimited file persistence
            ├── util/
            │   └── VehicleFactory.java      # Factory & input validator
            ├── web/
            │   └── SmartParkingWebServer.java # Built-in JDK HTTP Server (Port 8080)
            └── test/
                ├── Phase1Test.java          # Core models & polymorphism (26 tests)
                ├── Phase2Test.java          # Slot management & capacity (28 tests)
                ├── Phase3Test.java          # Vehicle entry & ticket IDs (28 tests)
                ├── Phase4Test.java          # Exit, duration ceiling, bills (30 tests)
                ├── Phase5Test.java          # File persistence & restart (28 tests)
                └── Phase6Test.java          # End-to-end integration (27 tests)
```

---

## ⚙️ Prerequisites

- **Java Development Kit (JDK)**: Version 8 or above (JDK 8, 11, 17, 21, or newer).
- Verify your Java installation:
  ```bash
  java -version
  javac -version
  ```

---

## 🚀 Running on a New Device (Step-by-Step)

### 1. Windows (CMD / PowerShell)

#### Step 1: Clone or Download the Repository
```powershell
git clone https://github.com/<your-username>/javaParking.git
cd javaParking
```

#### Step 2: One-Click Run (Easiest)
Double-click `run.bat` or run:
```powershell
.\run.bat
```
*(Or via PowerShell: `.\run.ps1`)*

#### Step 3: Manual Compilation & Execution
```powershell
# Create build directory
if not exist bin mkdir bin

# Compile all source files
javac -d bin src/com/smartparking/*.java src/com/smartparking/model/*.java src/com/smartparking/exception/*.java src/com/smartparking/service/*.java src/com/smartparking/util/*.java src/com/smartparking/web/*.java src/com/smartparking/test/*.java

# Run the Main Application
java -cp bin com.smartparking.ParkingManager
```

---

### 2. macOS / Linux

#### Step 1: Clone the Repository
```bash
git clone https://github.com/<your-username>/javaParking.git
cd javaParking
```

#### Step 2: One-Click Run
```bash
chmod +x run.sh test.sh
./run.sh
```

#### Step 3: Manual Compilation & Execution
```bash
# Create build directory
mkdir -p bin

# Compile all source files
javac -d bin src/com/smartparking/*.java \
             src/com/smartparking/model/*.java \
             src/com/smartparking/exception/*.java \
             src/com/smartparking/service/*.java \
             src/com/smartparking/util/*.java \
             src/com/smartparking/web/*.java \
             src/com/smartparking/test/*.java

# Run the Main Application
java -cp bin com.smartparking.ParkingManager
```

---

## 🧪 Running the Automated Test Suites (167 Tests)

The project includes **6 comprehensive unit and integration test suites** asserting 167 conditions:

#### On Windows:
```powershell
.\test.bat
```

#### On Linux / macOS:
```bash
./test.sh
```

#### Individual Test Suites:
```bash
java -cp bin com.smartparking.test.Phase1Test   # 26 Tests: Models & Polymorphism
java -cp bin com.smartparking.test.Phase2Test   # 28 Tests: Slot Allocation & Release
java -cp bin com.smartparking.test.Phase3Test   # 28 Tests: Entry & Ticket Generation
java -cp bin com.smartparking.test.Phase4Test   # 30 Tests: Exit & Ceiling Duration
java -cp bin com.smartparking.test.Phase5Test   # 28 Tests: File Persistence & Restart
java -cp bin com.smartparking.test.Phase6Test   # 27 Tests: 14 End-to-End Scenarios
```

---

## 🌐 Optional: Interactive Visual Web Dashboard

In addition to the console interface, the project includes an interactive web dashboard for demonstrations:

### Method A: Open Directly in Browser (No Server Required)
Open `web/index.html` in Chrome, Edge, Safari, or Firefox:
- **Windows**: `Start-Process web/index.html`
- **macOS**: `open web/index.html`
- **Linux**: `xdg-open web/index.html`

### Method B: Launch via Built-in Java Web Server
The project includes a lightweight HTTP server using `com.sun.net.httpserver.HttpServer` (zero external dependencies):
```bash
java -cp bin com.smartparking.web.SmartParkingWebServer
```
Open your browser at: **`http://localhost:8080`**

---

## 📚 Academic Documentation & Viva Material

- **[PROJECT_REPORT.md](PROJECT_REPORT.md)**: Formal academic project documentation including problem statement, UML class diagram, system methodology, OOP code mappings, and lab evaluation script.
- **[VIVA_QUESTIONS.md](VIVA_QUESTIONS.md)**: Top 20 Java viva questions & model answers covering Polymorphism, Encapsulation, Abstract classes, Checked exceptions, and File I/O.
- **[PROJECT_PROGRESS.md](PROJECT_PROGRESS.md)**: Phase-wise progression log from initial requirements analysis to final delivery.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
