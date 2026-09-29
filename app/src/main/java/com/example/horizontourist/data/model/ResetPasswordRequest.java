package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("token")
    public String token;

    @SerializedName("newPassword")
    public String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String token, String newPassword) {
        this.token = token;
        this.newPassword = newPassword;
    }
}
