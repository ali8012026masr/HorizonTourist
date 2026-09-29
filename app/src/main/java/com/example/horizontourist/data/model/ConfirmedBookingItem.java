package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class ConfirmedBookingItem {

    @SerializedName("bookingId")
    public Long bookingId;

    @SerializedName("title")
    public String title;

    @SerializedName("amount")
    public double amount;

    @SerializedName("status")
    public String status;

    public ConfirmedBookingItem() {
    }

    public ConfirmedBookingItem(Long bookingId, String title, double amount, String status) {
        this.bookingId = bookingId;
        this.title = title;
        this.amount = amount;
        this.status = status;
    }
}
