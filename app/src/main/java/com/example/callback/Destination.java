package com.example.callback;

public class Destination {
    String id;
    String vehicleCode;
    com.google.android.gms.maps.model.LatLng destinations;
    String locationTag;

    public Destination() {
    }

    public Destination(String id, String vehicleCode, com.google.android.gms.maps.model.LatLng destinations, String locationTag) {
        this.id = id;
        this.vehicleCode = vehicleCode;
        this.destinations = destinations;
        this.locationTag = locationTag;
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

    public com.google.android.gms.maps.model.LatLng getDestinations() {
        return destinations;
    }

    public void setDestinations(com.google.android.gms.maps.model.LatLng destinations) {
        this.destinations = destinations;
    }

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }
}

