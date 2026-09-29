package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("tourGuideId")
    public Long tourGuideId;

    @SerializedName("latitude")
    public double latitude;

    @SerializedName("longitude")
    public double longitude;

    @SerializedName("updatedAt")
    public String updatedAt;

    public Location() {
    }
}
