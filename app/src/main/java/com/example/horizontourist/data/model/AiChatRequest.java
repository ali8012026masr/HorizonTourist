package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AiChatRequest {
    @SerializedName("userMessage")
    public String userMessage;

    @SerializedName("history")
    public List<AiChatMessage> history;

    public AiChatRequest() {
    }

    public AiChatRequest(String userMessage, List<AiChatMessage> history) {
        this.userMessage = userMessage;
        this.history = history;
    }
}
