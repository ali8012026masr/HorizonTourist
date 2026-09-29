package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class CountResponse {
    @SerializedName("count")
    public int count;

    public CountResponse() {
    }

    public CountResponse(int count) {
        this.count = count;
    }
}
