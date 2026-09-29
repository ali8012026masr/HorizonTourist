package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideRating {
    @SerializedName("id")
    public Long id;

    @SerializedName("guideBookingId")
    public Long guideBookingId;

    @SerializedName("score")
    public int score;

    @SerializedName("comment")
    public String comment;

    @SerializedName("ratedDate")
    public String ratedDate;

    public GuideRating() {
    }
}
