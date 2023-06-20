package com.example.callback;

public class Destination {
    String id;
    String vehicleCode;
    double latitude;
    double longitude;
    String tagLocation;

    public Destination() {
    }

    public Destination(String id, double latitude, double longitude,  String tagLocation,String vehicleCode) {
        this.id = id;
        this.vehicleCode = vehicleCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tagLocation = tagLocation;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTagLocation() {
        return tagLocation;
    }

    public void setTagLocation(String tagLocation) {
        this.tagLocation = tagLocation;
    }
}

