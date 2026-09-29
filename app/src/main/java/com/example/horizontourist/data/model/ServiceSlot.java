package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceSlot {
    @SerializedName("id")
    public Long id;

    @SerializedName("providerId")
    public Long providerId;

    @SerializedName("providerBusinessName")
    public String providerBusinessName;

    @SerializedName("category")
    public String category;

    @SerializedName("origin")
    public String origin;

    @SerializedName("destination")
    public String destination;

    @SerializedName("locationName")
    public String locationName;

    @SerializedName("startDateTime")
    public String startDateTime;

    @SerializedName("endDateTime")
    public String endDateTime;

    @SerializedName("capacity")
    public Integer capacity;

    @SerializedName("availableSeats")
    public Integer availableSeats;

    @SerializedName("price")
    public double price;

    @SerializedName("status")
    public String status;

    @SerializedName("providerRatingAvg")
    public Double providerRatingAvg;

    @SerializedName("providerRatingCount")
    public Integer providerRatingCount;

    public ServiceSlot() {
    }

    public boolean isOpen() {
        return Constants.SLOT_STATUS_OPEN.equalsIgnoreCase(status);
    }
}
