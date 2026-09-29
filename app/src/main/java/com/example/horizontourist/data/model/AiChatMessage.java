package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class AiChatMessage {
    @SerializedName("role")
    public String role;

    @SerializedName("text")
    public String text;

    public AiChatMessage() {
    }

    public AiChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
    }
}
