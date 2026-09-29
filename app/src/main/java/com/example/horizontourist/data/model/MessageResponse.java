package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class MessageResponse {
    @SerializedName("message")
    public String message;

    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }
}
