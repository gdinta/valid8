package com.valid8.model;

/**
 * JSON response object for /api/summary (one per lot).
 */
public class LotSummary {

    private String lotName;
    private int capacity;
    private int inLotNow;
    private int availableNow;
    private int totalSessions;
    private int violationsCount;
    private double occupancyPercent;
    private double latitude;
    private double longitude;
    private String availabilityColor;

    public LotSummary() {}

    // --- Getters and setters ---

    public String getLotName()           { return lotName; }
    public void setLotName(String v)     { this.lotName = v; }

    public int getCapacity()             { return capacity; }
    public void setCapacity(int v)       { this.capacity = v; }

    public int getInLotNow()             { return inLotNow; }
    public void setInLotNow(int v)       { this.inLotNow = v; }

    public int getAvailableNow()         { return availableNow; }
    public void setAvailableNow(int v)   { this.availableNow = v; }

    public int getTotalSessions()        { return totalSessions; }
    public void setTotalSessions(int v)  { this.totalSessions = v; }

    public int getViolationsCount()      { return violationsCount; }
    public void setViolationsCount(int v){ this.violationsCount = v; }

    public double getOccupancyPercent()        { return occupancyPercent; }
    public void setOccupancyPercent(double v)  { this.occupancyPercent = v; }

    public double getLatitude()          { return latitude; }
    public void setLatitude(double v)    { this.latitude = v; }

    public double getLongitude()         { return longitude; }
    public void setLongitude(double v)   { this.longitude = v; }

    public String getAvailabilityColor()        { return availabilityColor; }
    public void setAvailabilityColor(String v)  { this.availabilityColor = v; }
}
