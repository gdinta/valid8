package com.valid8.model;

/**
 * JSON response object for /api/violations (one per violation record).
 * Shows time in and time out instead of duration.
 */
public class ViolationItem {

    private String licensePlate;
    private String lotName;
    private String timeIn;   // formatted entry time
    private String timeOut;  // formatted exit time, or "Still In Lot"
    private String status;   // "Still In Lot" or "Left"

    public ViolationItem() {}

    public ViolationItem(String licensePlate, String lotName,
                         String timeIn, String timeOut, String status) {
        this.licensePlate = licensePlate;
        this.lotName      = lotName;
        this.timeIn       = timeIn;
        this.timeOut      = timeOut;
        this.status       = status;
    }

    public String getLicensePlate()           { return licensePlate; }
    public void setLicensePlate(String v)     { this.licensePlate = v; }

    public String getLotName()                { return lotName; }
    public void setLotName(String v)          { this.lotName = v; }

    public String getTimeIn()                 { return timeIn; }
    public void setTimeIn(String v)           { this.timeIn = v; }

    public String getTimeOut()                { return timeOut; }
    public void setTimeOut(String v)          { this.timeOut = v; }

    public String getStatus()                 { return status; }
    public void setStatus(String v)           { this.status = v; }
}
