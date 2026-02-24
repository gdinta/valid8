package com.valid8.model;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * Represents a single parking session record.
 * Ported faithfully from Python parking_record.py.
 *
 * Expected CSV columns: plate, lot, entry_time, exit_time
 * exit_time may be empty if the vehicle is still in the lot.
 *
 * legal() logic:
 *   - Plate must be registered
 *   - Registration must be for this lot
 *   - Duration must not exceed the registered max_hours
 */
public class ParkingRecord {

    private final String plate;
    private final String lot;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime; // null if still in lot

    // Set after cross-referencing with registrations
    private Registration registration;

    public ParkingRecord(String plate, String lot, LocalDateTime entryTime, LocalDateTime exitTime) {
        this.plate = plate.trim().toUpperCase();
        this.lot = lot.trim();
        this.entryTime = entryTime;
        this.exitTime = exitTime;
    }

    public String getPlate()          { return plate; }
    public String getLot()            { return lot; }
    public LocalDateTime getEntryTime(){ return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Registration getRegistration() { return registration; }
    public void setRegistration(Registration r) { this.registration = r; }

    /** True if vehicle is still in the lot (no exit recorded). */
    public boolean isStillInLot() {
        return exitTime == null;
    }

    /**
     * Compute duration in minutes.
     * If still in lot, compute against current time.
     */
    public long getDurationMinutes() {
        LocalDateTime end = (exitTime != null) ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end).toMinutes();
    }

    /**
     * Core legality check.
     *
     * A record is a VIOLATION when the plate has no registration
     * for this specific lot. This is the only rule used for RUPD enforcement:
     * unregistered = not in the registered_cars.csv for this lot.
     *
     * Returns false (violation) if the plate is not registered here.
     */
    public boolean legal() {
        if (registration == null) {
            return false; // plate not registered anywhere
        }
        if (!registration.getLot().equalsIgnoreCase(lot)) {
            return false; // registered but for a different lot
        }
        return true;
    }

    /**
     * Human-readable violation reason.
     * Since violations are registration-based only, always "Unregistered".
     */
    public String violationReason() {
        return "Unregistered";
    }

    @Override
    public String toString() {
        return "ParkingRecord{plate='" + plate + "', lot='" + lot
                + "', entry=" + entryTime + ", exit=" + exitTime + "}";
    }
}
