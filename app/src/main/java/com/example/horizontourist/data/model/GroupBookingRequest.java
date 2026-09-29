package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupBookingRequest {
    @SerializedName("bookingId")
    public Long bookingId;

    public GroupBookingRequest() {
    }

    public GroupBookingRequest(Long bookingId) {
        this.bookingId = bookingId;
    }
}
