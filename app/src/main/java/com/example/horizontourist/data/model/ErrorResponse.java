package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ErrorResponse {
    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("status")
    public int status;

    @SerializedName("message")
    public String message;

    @SerializedName("errors")
    public Map<String, String> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
