package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterTouristRequest {
    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("phone")
    public String phone;

    @SerializedName("address")
    public String address;

    public RegisterTouristRequest() {
    }

    public RegisterTouristRequest(String fullName, String email, String password, String phone, String address) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
    }
}
