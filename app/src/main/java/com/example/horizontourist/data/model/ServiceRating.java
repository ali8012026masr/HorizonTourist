package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class ServiceRating {
    @SerializedName("id")
    public Long id;

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

    @SerializedName("ratedDate")
    public String ratedDate;

    public ServiceRating() {
    }
}
