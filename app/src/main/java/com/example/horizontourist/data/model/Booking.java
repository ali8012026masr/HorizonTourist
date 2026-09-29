package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    public Long id;

    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("serviceSlotId")
    public Long serviceSlotId;

    @SerializedName("bookingDate")
    public String bookingDate;

    @SerializedName("status")
    public String status;

    @SerializedName("totalAmount")
    public double totalAmount;

    @SerializedName("paymentStatus")
    public String paymentStatus;

    public Booking() {
    }
}
