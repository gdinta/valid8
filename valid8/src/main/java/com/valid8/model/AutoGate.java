package com.valid8.model;

import java.util.*;

/**
 * AutoGate — ported from Python autogate.py.
 *
 * Holds the lot configuration registry and applies business logic
 * to a set of ParkingRecords. Computes lot summaries, violations,
 * and unregistered lists.
 */
public class AutoGate {

    /**
     * Canonical lot configurations for Rutgers New Brunswick.
     * Coordinates are approximate demo values centered on campus areas.
     */
    public static final List<LotConfig> LOT_CONFIGS = List.of(
            new LotConfig("Busch Lot 51", 90, 40.5221, -74.4597),
            new LotConfig("Livingston Yellow", 85, 40.5239, -74.4495),
            new LotConfig("College Ave Deck", 100, 40.4983, -74.4480),
            new LotConfig("Cook Lot 98", 60, 40.4844, -74.4357),
            new LotConfig("Lot 33", 75, 40.5058, -74.4530));

    /**
     * Build a map of lotName -> LotConfig for fast lookup.
     */
    public static Map<String, LotConfig> buildConfigMap() {
        Map<String, LotConfig> map = new LinkedHashMap<>();
        for (LotConfig lc : LOT_CONFIGS) {
            map.put(lc.getName(), lc);
        }
        return map;
    }

    /**
     * Cross-reference each ParkingRecord against the registration map.
     * Sets the registration field on each record.
     *
     * @param records       All parking session records
     * @param registrations Map of plate -> Registration
     */
    public static void applyRegistrations(List<ParkingRecord> records,
            Map<String, Registration> registrations) {
        for (ParkingRecord pr : records) {
            Registration reg = registrations.get(pr.getPlate());
            pr.setRegistration(reg); // null if not found
        }
    }

    /**
     * Compute available spots for a lot.
     * available = capacity - currentInLot
     * Clamped to 0 minimum for display safety.
     */
    public static int computeAvailable(int capacity, int currentInLot) {
        return Math.max(0, capacity - currentInLot);
    }

    /**
     * Determine color coding based on available/capacity ratio.
     *
     * Green : available >= 40% of capacity
     * Yellow : available >= 15% and < 40%
     * Red : available < 15%
     */
    public static String availabilityColor(int available, int capacity) {
        if (capacity == 0)
            return "green";
        double ratio = (double) available / capacity;
        if (ratio >= 0.40)
            return "green";
        if (ratio >= 0.15)
            return "yellow";
        return "red";
    }
}
