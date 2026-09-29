package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupMember {
    @SerializedName("id")
    public Long id;

    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("touristName")
    public String touristName;

    @SerializedName("status")
    public String status;

    @SerializedName("joinedAt")
    public String joinedAt;

    @SerializedName("amountOwed")
    public double amountOwed;

    @SerializedName(value = "paid", alternate = {"isPaid"})
    public Boolean paid;

    public GroupMember() {
    }

    public boolean isPaid() {
        return Boolean.TRUE.equals(paid);
    }
}
