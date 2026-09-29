package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("id")
    public Long id;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("role")
    public String role;

    @SerializedName("roleDetails")
    public Tourist roleDetails;

    @SerializedName("accessToken")
    public String accessToken;

    @SerializedName("refreshToken")
    public String refreshToken;

    @SerializedName("expiresInSeconds")
    public Long expiresInSeconds;

    public LoginResponse() {
    }

    public Long getTouristId() {
        if (roleDetails != null && roleDetails.id != null) {
            return roleDetails.id;
        }
        return id;
    }
}
