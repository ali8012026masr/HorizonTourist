package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class GroupCreateRequest {
    @SerializedName("groupName")
    public String groupName;

    @SerializedName("createdByTouristId")
    public Long createdByTouristId;

    public GroupCreateRequest() {
    }

    public GroupCreateRequest(String groupName, Long createdByTouristId) {
        this.groupName = groupName;
        this.createdByTouristId = createdByTouristId;
    }
}
