package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class StatusRequest {
    @SerializedName("status")
    public String status;

    public StatusRequest() {
    }

    public StatusRequest(String status) {
        this.status = status;
    }
}
