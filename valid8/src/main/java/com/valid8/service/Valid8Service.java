package com.valid8.service;

import com.valid8.model.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core service that loads CSV data, applies AutoGate business logic,
 * and provides the computed summaries, violations, and unregistered lists.
 *
 * All data is cached in memory and refreshed on demand via reload().
 *
 * Live simulation: each reload applies a different random occupancy level
 * per lot so all three color states (green / yellow / red) appear across
 * the dashboard, simulating a realistic mix of busy and quiet lots.
 */
@Service
public class Valid8Service {

    @Autowired
    private CsvDataLoader csvDataLoader;

    private static final DateTimeFormatter DT_FMT =
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    // Cached data
    private List<ParkingRecord> records = new ArrayList<>();
    private Map<String, Registration> registrations = new LinkedHashMap<>();
    private LocalDateTime lastLoaded = null;
    private String loadError = null;

    // Per-lot live occupancy override (lotName -> absolute inLotNow value)
    // Set on each reload to produce a realistic mix of green/yellow/red lots.
    private final Map<String, Integer> lotSimulated = new LinkedHashMap<>();
    private final Random rng = new Random();

    @PostConstruct
    public void init() {
        reload();
    }

    // -----------------------------------------------------------------------
    // Reload
    // -----------------------------------------------------------------------

    /**
     * (Re)load CSV files and randomize simulated occupancy per lot.
     *
     * To guarantee visible color variety across lots on every reload,
     * each lot is independently assigned a random occupancy level drawn
     * from one of three bands:
     *   GREEN  band: 0–59% occupancy  (available >= 40%)
     *   YELLOW band: 61–84% occupancy (available 15–39%)
     *   RED    band: 87–99% occupancy (available < 15%)
     *
     * The band is chosen randomly per lot, so the dashboard will always
     * show a realistic spread of green, yellow, and red cards.
     */
    public synchronized void reload() {
        loadError = null;
        try {
            registrations = csvDataLoader.loadRegistrations();
            records = csvDataLoader.loadParkingRecords();
            AutoGate.applyRegistrations(records, registrations);

            for (LotConfig lc : AutoGate.LOT_CONFIGS) {
                int cap = lc.getCapacity();
                // Pick a random color band for this lot
                int band = rng.nextInt(3); // 0=green, 1=yellow, 2=red
                int inLot;
                if (band == 0) {
                    // GREEN: 0–59% occupied → available >= 40%
                    int maxInLot = (int)(cap * 0.59);
                    inLot = rng.nextInt(Math.max(1, maxInLot + 1));
                } else if (band == 1) {
                    // YELLOW: 61–84% occupied → available 15–39%
                    int minInLot = (int)(cap * 0.61);
                    int maxInLot = (int)(cap * 0.84);
                    inLot = minInLot + rng.nextInt(Math.max(1, maxInLot - minInLot + 1));
                } else {
                    // RED: 87–99% occupied → available < 15%
                    int minInLot = (int)(cap * 0.87);
                    int maxInLot = (int)(cap * 0.99);
                    inLot = minInLot + rng.nextInt(Math.max(1, maxInLot - minInLot + 1));
                }
                lotSimulated.put(lc.getName(), Math.min(cap, inLot));
            }

            lastLoaded = LocalDateTime.now();
            System.out.println("[Valid8Service] Data reloaded at " + lastLoaded);
        } catch (Exception e) {
            loadError = e.getMessage();
            System.err.println("[Valid8Service] Reload error: " + e.getMessage());
        }
    }

    public LocalDateTime getLastLoaded() { return lastLoaded; }
    public String getLoadError()         { return loadError; }

    // -----------------------------------------------------------------------
    // Lot Summaries
    // -----------------------------------------------------------------------

    public List<LotSummary> getLotSummaries() {
        List<LotSummary> summaries = new ArrayList<>();

        for (LotConfig lc : AutoGate.LOT_CONFIGS) {
            List<ParkingRecord> lotRecords = records.stream()
                .filter(r -> r.getLot().equalsIgnoreCase(lc.getName()))
                .collect(Collectors.toList());

            // Use simulated occupancy so numbers change visibly on reload
            int inLotNow = lotSimulated.getOrDefault(lc.getName(),
                (int) lotRecords.stream().filter(ParkingRecord::isStillInLot).count());

            int availableNow = AutoGate.computeAvailable(lc.getCapacity(), inLotNow);

            long violationsCount = lotRecords.stream()
                .filter(r -> !r.legal())
                .count();

            double occupancyPct = lc.getCapacity() > 0
                ? (double) inLotNow / lc.getCapacity() * 100.0
                : 0.0;

            String color = AutoGate.availabilityColor(availableNow, lc.getCapacity());

            LotSummary s = new LotSummary();
            s.setLotName(lc.getName());
            s.setCapacity(lc.getCapacity());
            s.setInLotNow(inLotNow);
            s.setAvailableNow(availableNow);
            s.setTotalSessions(lotRecords.size());
            s.setViolationsCount((int) violationsCount);
            s.setOccupancyPercent(Math.round(occupancyPct * 10.0) / 10.0);
            s.setLatitude(lc.getLatitude());
            s.setLongitude(lc.getLongitude());
            s.setAvailabilityColor(color);

            summaries.add(s);
        }
        return summaries;
    }

    // -----------------------------------------------------------------------
    // Violations — only unregistered plates
    // -----------------------------------------------------------------------

    /**
     * Return all records where the plate is not registered for this lot.
     * Shows time in and time out (no duration).
     */
    public List<ViolationItem> getViolations() {
        return records.stream()
            .filter(r -> !r.legal())
            .map(r -> new ViolationItem(
                r.getPlate(),
                r.getLot(),
                r.getEntryTime() != null ? r.getEntryTime().format(DT_FMT) : "—",
                r.getExitTime()  != null ? r.getExitTime().format(DT_FMT)  : "Still In Lot",
                r.isStillInLot() ? "Still In Lot" : "Left"
            ))
            .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Unregistered — each session for an unregistered plate, with times
    // -----------------------------------------------------------------------

    public List<UnregisteredItem> getUnregistered() {
        return records.stream()
            .filter(r -> r.getRegistration() == null)
            .map(r -> new UnregisteredItem(
                r.getPlate(),
                r.getLot(),
                r.getEntryTime() != null ? r.getEntryTime().format(DT_FMT) : "—",
                r.getExitTime()  != null ? r.getExitTime().format(DT_FMT)  : "Still In Lot"
            ))
            .collect(Collectors.toList());
    }
}
