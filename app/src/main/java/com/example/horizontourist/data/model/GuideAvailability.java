package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideAvailability {
    @SerializedName("id")
    public Long id;

    @SerializedName("tourGuideId")
    public Long tourGuideId;

    @SerializedName("tourGuideName")
    public String tourGuideName;

    @SerializedName("date")
    public String date;

    @SerializedName("startTime")
    public String startTime;

    @SerializedName("endTime")
    public String endTime;

    @SerializedName("location")
    public String location;

    @SerializedName("notes")
    public String notes;

    @SerializedName("status")
    public String status;

    @SerializedName("tourGuideRatingAvg")
    public Double tourGuideRatingAvg;

    @SerializedName("tourGuideRatingCount")
    public Integer tourGuideRatingCount;

    public GuideAvailability() {
    }

    public boolean isOpen() {
        return Constants.AVAILABILITY_STATUS_OPEN.equalsIgnoreCase(status);
    }
}
