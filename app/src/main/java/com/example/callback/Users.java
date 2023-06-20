package com.example.callback;

import androidx.annotation.Nullable;

public class Users {
    String UserId;
    String name;
    String email;
    String phone;
    boolean vehicle;
    String vehicleRegistration;
    String vehicleCode;

    public Users() {

    }

    public Users(String UserId, String name, String email, String phone, boolean vehicle, @Nullable String vehicleRegistration,@Nullable String vehicleCode) {
        this.UserId = UserId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.vehicle = vehicle;
        this.vehicleRegistration = vehicleRegistration;
        this.vehicleCode =vehicleCode;
    }
    public String getUserId() {return UserId;}

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isVehicle() {
        return vehicle;
    }

    public void setVehicle(boolean vehicle) {
        this.vehicle = vehicle;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;

    }
    public String getVehicleCode() {
        return vehicleCode;
    }

    public void setVehicleCode(String vehicleCode) {
        this.vehicleCode = vehicleCode;
    }
}
