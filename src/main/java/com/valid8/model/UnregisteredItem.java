package com.valid8.model;

/**
 * JSON response object for /api/unregistered.
 * Includes time in and time out for RUPD display.
 */
public class UnregisteredItem {

    private String licensePlate;
    private String lotName;
    private String timeIn;
    private String timeOut;

    public UnregisteredItem() {}

    public UnregisteredItem(String licensePlate, String lotName, String timeIn, String timeOut) {
        this.licensePlate = licensePlate;
        this.lotName      = lotName;
        this.timeIn       = timeIn;
        this.timeOut      = timeOut;
    }

    public String getLicensePlate()       { return licensePlate; }
    public void setLicensePlate(String v) { this.licensePlate = v; }

    public String getLotName()            { return lotName; }
    public void setLotName(String v)      { this.lotName = v; }

    public String getTimeIn()             { return timeIn; }
    public void setTimeIn(String v)       { this.timeIn = v; }

    public String getTimeOut()            { return timeOut; }
    public void setTimeOut(String v)      { this.timeOut = v; }
}
