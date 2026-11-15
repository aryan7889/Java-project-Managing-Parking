package com.smartparking.service;

import com.smartparking.model.ParkingTicket;
import com.smartparking.model.TicketStatus;
import com.smartparking.model.Vehicle;
import com.smartparking.model.VehicleType;
import com.smartparking.util.VehicleFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles text-based persistence for the Smart Parking System.
 * Uses core Java I/O (BufferedReader, BufferedWriter, FileWriter) and pipe-delimited
 * flat text files to preserve active parking sessions and completed history.
 */
public class FileStorageService {

    public static final String DEFAULT_DATA_DIR = "data";
    public static final String ACTIVE_TICKETS_FILENAME = "active_tickets.txt";
    public static final String PARKING_HISTORY_FILENAME = "parking_history.txt";
    public static final String DELIMITER = "\\|";
    public static final String SEPARATOR = "|";

    private final File dataDir;
    private final File activeTicketsFile;
    private final File parkingHistoryFile;

    /**
     * Default constructor: uses default "data" directory.
     */
    public FileStorageService() {
        this(DEFAULT_DATA_DIR);
    }

    /**
     * Parameterized constructor: allows specifying custom storage folder
     * (especially useful for automated unit and integration tests).
     */
    public FileStorageService(String dataDirPath) {
        this.dataDir = new File(dataDirPath);
        this.activeTicketsFile = new File(dataDir, ACTIVE_TICKETS_FILENAME);
        this.parkingHistoryFile = new File(dataDir, PARKING_HISTORY_FILENAME);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * Saves all currently active parking tickets to active_tickets.txt.
     * Rewrites file completely to reflect recent entries and departures.
     *
     * @param tickets Collection of active tickets
     * @throws IOException on I/O write failure
     */
    public synchronized void saveActiveTickets(Collection<ParkingTicket> tickets) throws IOException {
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(activeTicketsFile, false))) {
            for (ParkingTicket ticket : tickets) {
                if (ticket != null && ticket.getStatus() == TicketStatus.ACTIVE) {
                    writer.write(serializeActiveTicket(ticket));
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Loads all active parking tickets from active_tickets.txt.
     * Handles missing, empty, or corrupted files gracefully.
     *
     * @return List of reconstructed active ParkingTicket objects
     */
    public synchronized List<ParkingTicket> loadActiveTickets() {
        List<ParkingTicket> activeList = new ArrayList<>();
        if (!activeTicketsFile.exists() || activeTicketsFile.length() == 0) {
            return activeList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(activeTicketsFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                try {
                    ParkingTicket ticket = deserializeActiveTicket(line);
                    if (ticket != null) {
                        activeList.add(ticket);
                    }
                } catch (Exception e) {
                    System.err.printf("[Warning] Skipping malformed active ticket at line %d: %s (%s)%n",
                            lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to read active tickets file: " + e.getMessage());
        }

        return activeList;
    }

    /**
     * Appends a newly completed parking ticket to parking_history.txt.
     *
     * @param ticket Completed ticket to archive
     * @throws IOException on I/O write failure
     */
    public synchronized void appendHistoricalTicket(ParkingTicket ticket) throws IOException {
        if (ticket == null || ticket.getStatus() != TicketStatus.COMPLETED) {
            return;
        }
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(parkingHistoryFile, true))) {
            writer.write(serializeHistoricalTicket(ticket));
            writer.newLine();
        }
    }

    /**
     * Saves entire parking history collection (rewriting the file).
     */
    public synchronized void saveParkingHistory(Collection<ParkingTicket> history) throws IOException {
        ensureDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(parkingHistoryFile, false))) {
            for (ParkingTicket ticket : history) {
                if (ticket != null && ticket.getStatus() == TicketStatus.COMPLETED) {
                    writer.write(serializeHistoricalTicket(ticket));
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Loads all completed historical parking tickets from parking_history.txt.
     *
     * @return List of reconstructed historical ParkingTicket objects
     */
    public synchronized List<ParkingTicket> loadParkingHistory() {
        List<ParkingTicket> historyList = new ArrayList<>();
        if (!parkingHistoryFile.exists() || parkingHistoryFile.length() == 0) {
            return historyList;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(parkingHistoryFile))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                try {
                    ParkingTicket ticket = deserializeHistoricalTicket(line);
                    if (ticket != null) {
                        historyList.add(ticket);
                    }
                } catch (Exception e) {
                    System.err.printf("[Warning] Skipping malformed historical ticket at line %d: %s (%s)%n",
                            lineNumber, line, e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to read parking history file: " + e.getMessage());
        }

        return historyList;
    }

    /**
     * Helper to clear files in the storage folder (primarily used for test cleanup).
     */
    public synchronized void clearAllData() {
        if (activeTicketsFile.exists()) {
            activeTicketsFile.delete();
        }
        if (parkingHistoryFile.exists()) {
            parkingHistoryFile.delete();
        }
    }

    // =========================================================================
    // Format serialization & deserialization helpers
    // =========================================================================

    /**
     * Format: TicketID|VehicleType|RegNumber|OwnerName|SlotID|EntryTime
     */
    private String serializeActiveTicket(ParkingTicket t) {
        return String.join(SEPARATOR,
                t.getTicketId(),
                t.getVehicle().getVehicleType().name(),
                t.getVehicle().getRegistrationNumber(),
                t.getVehicle().getOwnerName(),
                t.getSlotId(),
                t.getEntryTime().toString()
        );
    }

    /**
     * Parses active ticket line.
     */
    private ParkingTicket deserializeActiveTicket(String line) {
        String[] tokens = line.split(DELIMITER);
        if (tokens.length < 6) {
            throw new IllegalArgumentException("Expected 6 tokens, found " + tokens.length);
        }

        String ticketId = tokens[0].trim();
        VehicleType type = VehicleType.valueOf(tokens[1].trim());
        String regNumber = tokens[2].trim();
        String ownerName = tokens[3].trim();
        String slotId = tokens[4].trim();
        LocalDateTime entryTime = LocalDateTime.parse(tokens[5].trim());

        Vehicle vehicle = VehicleFactory.createVehicle(regNumber, ownerName, type);
        return new ParkingTicket(ticketId, vehicle, slotId, entryTime);
    }

    /**
     * Format: TicketID|VehicleType|RegNumber|OwnerName|SlotID|EntryTime|ExitTime|DurationHours|FeeCharged
     */
    private String serializeHistoricalTicket(ParkingTicket t) {
        return String.join(SEPARATOR,
                t.getTicketId(),
                t.getVehicle().getVehicleType().name(),
                t.getVehicle().getRegistrationNumber(),
                t.getVehicle().getOwnerName(),
                t.getSlotId(),
                t.getEntryTime().toString(),
                t.getExitTime() != null ? t.getExitTime().toString() : "",
                String.valueOf(t.getDurationHours()),
                String.valueOf(t.getFeeCharged())
        );
    }

    /**
     * Parses historical ticket line.
     */
    private ParkingTicket deserializeHistoricalTicket(String line) {
        String[] tokens = line.split(DELIMITER);
        if (tokens.length < 9) {
            throw new IllegalArgumentException("Expected 9 tokens, found " + tokens.length);
        }

        String ticketId = tokens[0].trim();
        VehicleType type = VehicleType.valueOf(tokens[1].trim());
        String regNumber = tokens[2].trim();
        String ownerName = tokens[3].trim();
        String slotId = tokens[4].trim();
        LocalDateTime entryTime = LocalDateTime.parse(tokens[5].trim());
        LocalDateTime exitTime = LocalDateTime.parse(tokens[6].trim());
        long durationHours = Long.parseLong(tokens[7].trim());
        double feeCharged = Double.parseDouble(tokens[8].trim());

        Vehicle vehicle = VehicleFactory.createVehicle(regNumber, ownerName, type);
        return new ParkingTicket(ticketId, vehicle, slotId, entryTime, exitTime, durationHours, feeCharged, TicketStatus.COMPLETED);
    }

    public File getActiveTicketsFile() {
        return activeTicketsFile;
    }

    public File getParkingHistoryFile() {
        return parkingHistoryFile;
    }
}
