package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentRequest {
    @SerializedName("bookingId")
    public Long bookingId;

    @SerializedName("guideBookingId")
    public Long guideBookingId;

    @SerializedName("amount")
    public double amount;

    @SerializedName("method")
    public String method;

    public PaymentRequest() {
    }

    public PaymentRequest(Long bookingId, Long guideBookingId, double amount, String method) {
        this.bookingId = bookingId;
        this.guideBookingId = guideBookingId;
        this.amount = amount;
        this.method = method;
    }

    public static PaymentRequest forBooking(Long bookingId, double amount, String method) {
        return new PaymentRequest(bookingId, null, amount, method);
    }

    public static PaymentRequest forGuideBooking(Long guideBookingId, double amount, String method) {
        return new PaymentRequest(null, guideBookingId, amount, method);
    }
}
