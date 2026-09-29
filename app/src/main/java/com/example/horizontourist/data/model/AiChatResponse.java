package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class AiChatResponse {
    @SerializedName("replyText")
    public String replyText;

    @SerializedName("actionType")
    public String actionType;

    @SerializedName("actionPayloadJson")
    public String actionPayloadJson;

    public AiChatResponse() {
    }
}
