package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GuideRatingRequest {
    @SerializedName("guideBookingId")
    public Long guideBookingId;

    @SerializedName("score")
    public int score;

    @SerializedName("comment")
    public String comment;

    public GuideRatingRequest() {
    }

    public GuideRatingRequest(Long guideBookingId, int score, String comment) {
        this.guideBookingId = guideBookingId;
        this.score = score;
        this.comment = comment;
    }
}
