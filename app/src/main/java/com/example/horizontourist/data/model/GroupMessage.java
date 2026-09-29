package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupMessage {
    @SerializedName("id")
    public Long id;

    @SerializedName("senderId")
    public Long senderId;

    @SerializedName("senderName")
    public String senderName;

    @SerializedName("message")
    public String message;

    @SerializedName("sentAt")
    public String sentAt;

    public GroupMessage() {
    }
}
