package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class JoinGroupRequest {
    @SerializedName("joinCode")
    public String joinCode;

    @SerializedName("touristId")
    public Long touristId;

    public JoinGroupRequest() {
    }

    public JoinGroupRequest(String joinCode, Long touristId) {
        this.joinCode = joinCode;
        this.touristId = touristId;
    }
}
