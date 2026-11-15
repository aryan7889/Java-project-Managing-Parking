# 🚗 Smart Parking & Parking Slot Management System

[![Java](https://img.shields.io/badge/Java-8%2B%20%7C%2011%20%7C%2017%20%7C%2021-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![React](https://img.shields.io/badge/React-18.3-61DAFB?logo=react&logoColor=black)](https://react.dev/)
[![TailwindCSS](https://img.shields.io/badge/TailwindCSS-3.4-38B2AC?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-167%2F167%20Passing%20(100%25)-success)](src/com/smartparking/test/)

A complete **Smart Parking & Parking Slot Management System** featuring both a pure **Java OOP Console Application** (for academic lab evaluation) and a modern **Menu-Driven React Web Application** (for interactive visual presentation).

---

## 📌 Table of Contents
- [Project Overview](#-project-overview)
- [Dual-Mode Architecture](#-dual-mode-architecture-cli--react-web)
- [Key Features](#-key-features)
- [How to Run on ANY Device](#-how-to-run-on-any-device)
  - [1. Running the Menu-Driven React Web App](#1-running-the-menu-driven-react-web-app)
  - [2. Running the Core Java Console Application](#2-running-the-core-java-console-application)
- [Core OOP Concepts Demonstrated](#-core-oop-concepts-demonstrated)
- [Pricing & Duration Rules](#-pricing--duration-rules)
- [Running Automated Tests (167 Tests)](#-running-automated-tests-167-tests)
- [Project Directory Structure](#-project-directory-structure)
- [License](#-license)

---

## 📖 Project Overview

This project solves urban parking congestion through automated vehicle-to-slot allocation, sequential ticket generation, duration ceiling fee calculations, and persistent storage.

It provides two synchronized interfaces:
1. **Academic Console CLI (`ParkingManager.java`)**: 100% pure Java (JDK 8+), zero external libraries, menu-driven (options 1 to 10), adhering strictly to OOPS course constraints.
2. **Interactive React Web App (`frontend/` & `web/index.html`)**: A modern, menu-driven React 18 interface replicating the 10-option CLI navigation with a 2D parking lot visualizer, live status badges, pass/invoice modals, and an interactive virtual terminal console.

---

## 🔄 Dual-Mode Architecture (CLI + React Web)

| Feature | Java Console Application | React Web Application |
|---|---|---|
| **Primary Purpose** | Academic Viva / Lab Evaluation | Visual Presentation / Project Expo |
| **Tech Stack** | Java 8+, `java.io`, `java.time` | React 18, Tailwind CSS, Vite |
| **Interface** | Numbered Console Menu (1-10) | Interactive Sidebar + 2D Grid + Virtual CLI |
| **Duration Logic** | Partial-hour ceiling rule | Partial-hour ceiling rule |
| **Billing Engine** | Subclass Dynamic Method Dispatch | Polymorphic rate calculation (₹10/₹20/₹40) |
| **Persistence** | Pipe-delimited `.txt` files in `data/` | Browser `localStorage` + Exportable state |

---

## 🚀 How to Run on ANY Device

### 1. Running the Menu-Driven React Web App

You have two convenient ways to run the React interface:

#### ⚡ Method A: Instant Zero-Install Run (Recommended)
You do **not** need to install `npm` dependencies to run this. Simply open the self-contained React app:
- **Windows**: Double-click [`web/index.html`](web/index.html) or run in PowerShell:
  ```powershell
  Start-Process web/index.html
  ```
- **macOS**: `open web/index.html`
- **Linux**: `xdg-open web/index.html`
- **Or via Built-in Java Server (Port 8080)**:
  ```bash
  javac -d bin src/com/smartparking/web/*.java
  java -cp bin com.smartparking.web.SmartParkingWebServer
  # Open http://localhost:8080 in your browser
  ```

#### 🛠 Method B: Vite + React Dev Server
If you want to run the full Node.js / Vite development server:
```bash
# Navigate to frontend folder
cd frontend

# Install dependencies
npm install

# Start local dev server
npm run dev
```
Open **`http://localhost:3000`** in your browser.

---

### 2. Running the Core Java Console Application

#### On Windows (PowerShell or Command Prompt):
```powershell
# Quick One-Click Launcher:
.\run.bat

# Or manual compilation:
if not exist bin mkdir bin
javac -d bin src/com/smartparking/*.java src/com/smartparking/model/*.java src/com/smartparking/exception/*.java src/com/smartparking/service/*.java src/com/smartparking/util/*.java src/com/smartparking/test/*.java
java -cp bin com.smartparking.ParkingManager
```

#### On macOS / Linux (Terminal):
```bash
# Quick One-Click Launcher:
chmod +x run.sh test.sh
./run.sh

# Or manual compilation:
mkdir -p bin
javac -d bin src/com/smartparking/*.java \
             src/com/smartparking/model/*.java \
             src/com/smartparking/exception/*.java \
             src/com/smartparking/service/*.java \
             src/com/smartparking/util/*.java \
             src/com/smartparking/test/*.java
java -cp bin com.smartparking.ParkingManager
```

---

## ✨ 10-Option Menu-Driven Features

Both the CLI and React Web App provide the identical 10 menu options:

1. **Register Vehicle / Vehicle Entry**: Choose Bike (₹10/hr), Car (₹20/hr), or Truck (₹40/hr); validates plate and owner, prevents active duplicates, and generates a formatted parking entry pass.
2. **Vehicle Exit & Billing**: Lookup by Ticket ID or License Plate; calculates duration with the partial-hour ceiling rule, dynamically calculates fee, releases slot, and prints invoice.
3. **Display All Parking Slots**: Visual 2D overview of all 18 physical slots (`B-1..B-5`, `C-1..C-10`, `T-1..T-3`).
4. **Display Available Slots**: Filters and displays only vacant slots ready for allocation.
5. **Display Occupied Slots**: Filters and displays only occupied slots with vehicle number and owner details.
6. **Search Vehicle**: Case-insensitive lookup across active parked slots and completed historical sessions.
7. **Search Parking Ticket**: Detailed inspection of any ticket record by ticket ID.
8. **View Parking History**: Tabular log of all completed parking sessions with timestamps and fees.
9. **Display Parking Statistics**: Facility analytics showing occupancy percentage, category breakdown, and total revenue.
10. **Interactive Terminal CLI / Exit**: Full simulated terminal console directly in the web browser.

---

## 🧬 Core OOP Concepts Demonstrated

- **Encapsulation**: All fields in `Vehicle`, `ParkingSlot`, and `ParkingTicket` are `private`. Access is regulated via validated getters/setters.
- **Inheritance**: `Bike`, `Car`, and `Truck` extend the abstract `Vehicle` class, sharing registration number, owner name, and vehicle type.
- **Abstraction**: `Vehicle` defines `public abstract double calculateFee(long durationHours);`.
- **Polymorphism**: Dynamic method dispatch invokes the overridden fee method of `Bike`, `Car`, or `Truck` at runtime via a `Vehicle` reference.
- **Custom Exceptions**: Checked exceptions (`SlotUnavailableException`, `DuplicateVehicleException`, `InvalidTicketException`) handle domain error conditions.
- **File I/O (`java.io`)**: `FileStorageService` reads/writes pipe-delimited text files (`active_tickets.txt`, `parking_history.txt`) with crash-resilient parsing.

---

## 🧪 Running Automated Tests (167 Tests)

The project includes **6 comprehensive test suites** asserting 167 conditions:

```powershell
# Windows
.\test.bat

# Linux / macOS
./test.sh
```

All 6 test suites pass with **100% success rate (167 / 167 Passed)**.

---

## 📁 Project Directory Structure

```text
javaParking/
├── .gitignore                     # Git exclusions (bin/, node_modules/, temp files)
├── LICENSE                        # MIT License
├── README.md                      # Complete project documentation
├── PROJECT_PROGRESS.md            # Phase-by-phase development log
├── PROJECT_REPORT.md              # Academic documentation & viva report
├── VIVA_QUESTIONS.md              # Top 20 Java viva questions & model answers
├── run.bat / run.ps1              # One-click Windows CLI launchers
├── run.sh                         # One-click Linux/macOS launcher
├── test.bat / test.sh             # One-click test runners (167 tests)
├── data/                          # Persistent text file storage directory
│   └── .gitkeep                   # Preserves data/ folder in Git
├── web/                           # Instant Zero-Install React Web UI
│   └── index.html                 # Standalone React 18 Web Dashboard
├── frontend/                      # Full Vite + React + Tailwind Source
│   ├── package.json               # Node.js dependencies
│   ├── vite.config.js             # Vite configuration
│   ├── tailwind.config.js         # Tailwind configuration
│   ├── index.html                 # Frontend HTML template
│   └── src/
│       ├── main.jsx               # React DOM entry point
│       ├── App.jsx                # Menu-Driven React application
│       └── index.css              # Tailwind CSS styles
└── src/com/smartparking/          # Pure Java OOP source code & 6 test suites
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
