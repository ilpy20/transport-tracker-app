package com.example.stopmodel;

public class Stop {
  private String stopId;
  private String stopName;
  private String stopCode;
  private String zoneId;
  private String vehicleMode;
  private int platformCode;

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopName(String stopName) {
    this.stopName = stopName;

  }

  public String getStopName() {
    return stopName;
  }

  public void setStopCode(String stopCode) {
    this.stopCode = stopCode;
  }

  public String getStopCode() {
    return stopCode;
  }

  public void setZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

  public String getZoneId() {
    return zoneId;
  }

  public void setVehicleMode(String vehicleMode) {
    this.vehicleMode = vehicleMode;
  }

  public String getVehicleMode() {
    return vehicleMode;
  }

  public void setPlatformCode(int platformCode) {
    this.platformCode = platformCode;
  }

  public int getPlatformCode() {
    return platformCode;
  }
}
