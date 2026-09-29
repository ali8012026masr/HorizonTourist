package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideBooking {
    @SerializedName("id")
    public Long id;

    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("tourGuideId")
    public Long tourGuideId;

    @SerializedName("guideAvailabilityId")
    public Long guideAvailabilityId;

    @SerializedName("scheduleDate")
    public String scheduleDate;

    @SerializedName("agreedPrice")
    public double agreedPrice;

    @SerializedName(value = "isNegotiated", alternate = {"negotiated"})
    public Boolean isNegotiated;

    @SerializedName("status")
    public String status;

    @SerializedName(value = "paymentReceived", alternate = {"isPaymentReceived"})
    public Boolean paymentReceived;

    public GuideBooking() {
    }

    public boolean isAccepted() {
        return Constants.GUIDE_BOOKING_STATUS_ACCEPTED.equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return Constants.GUIDE_BOOKING_STATUS_COMPLETED.equalsIgnoreCase(status);
    }

    public boolean isPaid() {
        return Boolean.TRUE.equals(paymentReceived);
    }
}
