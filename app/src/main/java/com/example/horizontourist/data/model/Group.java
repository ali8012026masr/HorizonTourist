package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Group {
    @SerializedName("id")
    public Long id;

    @SerializedName("groupName")
    public String groupName;

    @SerializedName("createdByTouristId")
    public Long createdByTouristId;

    @SerializedName("joinCode")
    public String joinCode;

    @SerializedName("bookingId")
    public Long bookingId;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("members")
    public List<GroupMember> members;

    public Group() {
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }
}
