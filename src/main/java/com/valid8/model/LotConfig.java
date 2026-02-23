package com.valid8.model;

/**
 * Immutable configuration for a parking lot.
 * Includes demo GPS coordinates for the Leaflet map.
 */
public class LotConfig {

    private final String name;
    private final int capacity;
    private final double latitude;
    private final double longitude;

    public LotConfig(String name, int capacity, double latitude, double longitude) {
        this.name = name;
        this.capacity = capacity;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName()     { return name; }
    public int getCapacity()    { return capacity; }
    public double getLatitude() { return latitude; }
    public double getLongitude(){ return longitude; }
}
