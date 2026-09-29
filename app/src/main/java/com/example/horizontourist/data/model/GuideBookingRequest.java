package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideBookingRequest {
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

    public GuideBookingRequest() {
    }

    public GuideBookingRequest(Long touristId, Long tourGuideId, Long guideAvailabilityId,
                               String scheduleDate, double agreedPrice, Boolean isNegotiated) {
        this.touristId = touristId;
        this.tourGuideId = tourGuideId;
        this.guideAvailabilityId = guideAvailabilityId;
        this.scheduleDate = scheduleDate;
        this.agreedPrice = agreedPrice;
        this.isNegotiated = isNegotiated;
    }
}
