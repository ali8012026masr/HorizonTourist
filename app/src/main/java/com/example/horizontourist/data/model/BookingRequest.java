package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class BookingRequest {
    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("serviceSlotId")
    public Long serviceSlotId;

    public BookingRequest() {
    }

    public BookingRequest(Long touristId, Long serviceSlotId) {
        this.touristId = touristId;
        this.serviceSlotId = serviceSlotId;
    }
}
