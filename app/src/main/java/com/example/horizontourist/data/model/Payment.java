package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class Payment {
    @SerializedName("id")
    public Long id;

    @SerializedName("bookingId")
    public Long bookingId;

    @SerializedName("guideBookingId")
    public Long guideBookingId;

    @SerializedName("amount")
    public double amount;

    @SerializedName("method")
    public String method;

    @SerializedName("status")
    public String status;

    @SerializedName("transactionDate")
    public String transactionDate;

    public Payment() {
    }
}
