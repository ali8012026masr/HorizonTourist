package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class Tourist {
    @SerializedName("id")
    public Long id;

    @SerializedName("fullName")
    public String fullName;

    @SerializedName("email")
    public String email;

    @SerializedName("phone")
    public String phone;

    @SerializedName("address")
    public String address;

    @SerializedName("status")
    public String status;

    @SerializedName("loyaltyPoints")
    public Integer loyaltyPoints;

    public Tourist() {
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public Integer getLoyaltyPoints() {
        return loyaltyPoints != null ? loyaltyPoints : 0;
    }
}
