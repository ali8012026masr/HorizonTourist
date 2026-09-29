package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateTouristRequest {
    @SerializedName("fullName")
    public String fullName;

    @SerializedName("phone")
    public String phone;

    @SerializedName("address")
    public String address;

    public UpdateTouristRequest() {
    }

    public UpdateTouristRequest(String fullName, String phone, String address) {
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
    }
}
