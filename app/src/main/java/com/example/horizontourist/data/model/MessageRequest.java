package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class MessageRequest {
    @SerializedName("touristId")
    public Long touristId;

    @SerializedName("message")
    public String message;

    public MessageRequest() {
    }

    public MessageRequest(Long touristId, String message) {
        this.touristId = touristId;
        this.message = message;
    }
}
