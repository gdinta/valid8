package com.valid8.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.valid8.model.ParkingRecord;
import com.valid8.model.Registration;
import com.valid8.util.TimeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Loads and parses the two CSV files.
 *
 * registered_cars.csv  — columns: plate, lot, max_hours
 * simulation_data.csv  — columns: plate, lot, entry_time, exit_time
 *
 * Files are resolved in this priority order:
 *   1. Absolute path as configured
 *   2. Relative to current working directory
 *   3. From the classpath (src/main/resources)
 */
@Component
public class CsvDataLoader {

    @Value("${valid8.csv.registrations:registered_cars.csv}")
    private String registrationsPath;

    @Value("${valid8.csv.simulation:simulation_data.csv}")
    private String simulationPath;

    // -----------------------------------------------------------------------
    // Public load methods
    // -----------------------------------------------------------------------

    /**
     * Load all registrations from registered_cars.csv.
     * Returns map of uppercase plate -> Registration.
     */
    public Map<String, Registration> loadRegistrations() {
        Map<String, Registration> map = new LinkedHashMap<>();
        List<String[]> rows = readCsv(registrationsPath);
        if (rows == null) return map;

        int[] idx = detectRegistrationColumns(rows.get(0));
        if (idx == null) {
            System.err.println("[CsvDataLoader] registered_cars.csv: cannot detect required columns (plate, lot, max_hours)");
            return map;
        }

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            try {
                String plate   = cell(row, idx[0]).toUpperCase();
                String lot     = cell(row, idx[1]);
                double maxHrs  = Double.parseDouble(cell(row, idx[2]));
                if (!plate.isBlank()) {
                    map.put(plate, new Registration(plate, lot, maxHrs));
                }
            } catch (NumberFormatException e) {
                System.err.println("[CsvDataLoader] Skipping registration row " + i + ": bad max_hours");
            }
        }
        System.out.println("[CsvDataLoader] Loaded " + map.size() + " registrations.");
        return map;
    }

    /**
     * Load all parking records from simulation_data.csv.
     * Returns list of ParkingRecord (registration not yet linked).
     */
    public List<ParkingRecord> loadParkingRecords() {
        List<ParkingRecord> list = new ArrayList<>();
        List<String[]> rows = readCsv(simulationPath);
        if (rows == null) return list;

        int[] idx = detectSimulationColumns(rows.get(0));
        if (idx == null) {
            System.err.println("[CsvDataLoader] simulation_data.csv: cannot detect required columns (plate, lot, entry_time, exit_time)");
            return list;
        }

        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            try {
                String plate       = cell(row, idx[0]).toUpperCase();
                String lot         = cell(row, idx[1]);
                LocalDateTime entry = TimeUtils.parse(cell(row, idx[2]));
                LocalDateTime exit  = TimeUtils.parse(cell(row, idx[3])); // null if blank

                if (!plate.isBlank() && entry != null) {
                    list.add(new ParkingRecord(plate, lot, entry, exit));
                }
            } catch (Exception e) {
                System.err.println("[CsvDataLoader] Skipping simulation row " + i + ": " + e.getMessage());
            }
        }
        System.out.println("[CsvDataLoader] Loaded " + list.size() + " parking records.");
        return list;
    }

    // -----------------------------------------------------------------------
    // Column detection (flexible header matching)
    // -----------------------------------------------------------------------

    /** Returns [plateIdx, lotIdx, maxHoursIdx] or null */
    private int[] detectRegistrationColumns(String[] header) {
        int plateIdx = -1, lotIdx = -1, maxHrsIdx = -1;
        for (int i = 0; i < header.length; i++) {
            String h = header[i].trim().toLowerCase();
            if (h.contains("plate") || h.equals("license"))   plateIdx  = i;
            else if (h.contains("lot"))                        lotIdx    = i;
            else if (h.contains("max") || h.contains("hour")) maxHrsIdx = i;
        }
        if (plateIdx < 0 || lotIdx < 0 || maxHrsIdx < 0) return null;
        return new int[]{plateIdx, lotIdx, maxHrsIdx};
    }

    /** Returns [plateIdx, lotIdx, entryIdx, exitIdx] or null */
    private int[] detectSimulationColumns(String[] header) {
        int plateIdx = -1, lotIdx = -1, entryIdx = -1, exitIdx = -1;
        for (int i = 0; i < header.length; i++) {
            String h = header[i].trim().toLowerCase();
            if (h.contains("plate") || h.equals("license"))   plateIdx  = i;
            else if (h.contains("lot"))                        lotIdx    = i;
            else if (h.contains("entry") || h.contains("in")) entryIdx  = i;
            else if (h.contains("exit") || h.contains("out")) exitIdx   = i;
        }
        if (plateIdx < 0 || lotIdx < 0 || entryIdx < 0 || exitIdx < 0) return null;
        return new int[]{plateIdx, lotIdx, entryIdx, exitIdx};
    }

    // -----------------------------------------------------------------------
    // CSV file reading
    // -----------------------------------------------------------------------

    /** Read all rows from a CSV file. Returns null on failure. */
    private List<String[]> readCsv(String pathStr) {
        // Try file system first
        Path fsPath = Paths.get(pathStr);
        if (fsPath.toFile().exists()) {
            return readFromFile(fsPath.toFile());
        }
        // Try classpath
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(pathStr)) {
            if (is != null) {
                return readFromStream(is);
            }
        } catch (IOException e) {
            System.err.println("[CsvDataLoader] Classpath read error: " + e.getMessage());
        }
        System.err.println("[CsvDataLoader] File not found: " + pathStr
                + " (checked filesystem and classpath)");
        return null;
    }

    private List<String[]> readFromFile(File file) {
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return null;
            return rows;
        } catch (IOException | CsvException e) {
            System.err.println("[CsvDataLoader] Error reading " + file + ": " + e.getMessage());
            return null;
        }
    }

    private List<String[]> readFromStream(InputStream is) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return null;
            return rows;
        } catch (IOException | CsvException e) {
            System.err.println("[CsvDataLoader] Error reading stream: " + e.getMessage());
            return null;
        }
    }

    private String cell(String[] row, int idx) {
        if (idx < 0 || idx >= row.length) return "";
        return row[idx] == null ? "" : row[idx].trim();
    }
}
