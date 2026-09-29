package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    public Long id;

    @SerializedName("message")
    public String message;

    @SerializedName("type")
    public String type;

    @SerializedName(value = "read", alternate = {"isRead"})
    public Boolean read;

    @SerializedName("createdAt")
    public String createdAt;

    public Notification() {
    }

    public boolean isRead() {
        return Boolean.TRUE.equals(read);
    }
}
