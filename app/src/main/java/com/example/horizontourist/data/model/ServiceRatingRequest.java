package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceRatingRequest {
    @SerializedName("bookingId")
    public Long bookingId;

    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("providerId")
    public Long providerId;

    @SerializedName("score")
    public int score;

    @SerializedName("comment")
    public String comment;

    public ServiceRatingRequest() {
    }

    public ServiceRatingRequest(Long bookingId, Long touristId, Long providerId, int score, String comment) {
        this.bookingId = bookingId;
        this.touristId = touristId;
        this.providerId = providerId;
        this.score = score;
        this.comment = comment;
    }
}
