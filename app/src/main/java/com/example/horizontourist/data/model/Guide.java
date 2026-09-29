package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Guide {
    @SerializedName("id")
    public Long id;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("phone")
    public String phone;

    @SerializedName("address")
    public String address;

    @SerializedName("status")
    public String status;

    @SerializedName("nationalId")
    public String nationalId;

    @SerializedName("bio")
    public String bio;

    @SerializedName("ratingAvg")
    public Double ratingAvg;

    @SerializedName("ratingCount")
    public Integer ratingCount;

    @SerializedName(value = "isAvailable", alternate = {"available"})
    public Boolean isAvailable;

    @SerializedName("verificationStatus")
    public String verificationStatus;

    @SerializedName("rejectionReason")
    public String rejectionReason;

    @SerializedName("languages")
    public List<String> languages;

    @SerializedName("location")
    public String location;

    @SerializedName("experienceYears")
    public Integer experienceYears;

    @SerializedName("defaultPrice")
    public double defaultPrice;

    @SerializedName(value = "negotiable", alternate = {"isNegotiable", "isNegotiated"})
    public Boolean negotiable;

    public Guide() {
    }

    public boolean isAvailableNow() {
        return Boolean.TRUE.equals(isAvailable);
    }

    public boolean isNegotiable() {
        return Boolean.TRUE.equals(negotiable);
    }
}
