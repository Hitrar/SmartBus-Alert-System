package com.example.callback;

public class MyVehicle {
    String id;
    String vehiclePlate;
    String vehicleCode;

    public MyVehicle() {
    }

    public MyVehicle(String id, String vehiclePlate, String vehicleCode) {
        this.id = id;
        this.vehiclePlate = vehiclePlate;
        this.vehicleCode = vehicleCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public void setVehiclePlate(String vehiclePlate) {
        this.vehiclePlate = vehiclePlate;
    }

    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }
}

