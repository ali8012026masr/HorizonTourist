package com.example.horizontourist.data.model;

import com.google.gson.annotations.SerializedName;

public class CartItem {
    @SerializedName("slotId")
    public Long slotId;

    @SerializedName("title")
    public String title;

    @SerializedName("price")
    public double price;

    @SerializedName("quantity")
    public int quantity;

    @SerializedName("addedAt")
    public String addedAt;

    @SerializedName("category")
    public String category;

    @SerializedName("startDateTime")
    public String startDateTime;

    @SerializedName("availableSeats")
    public Integer availableSeats;

    @SerializedName("status")
    public String status;

    public CartItem() {
        this.quantity = 1;
    }

    public CartItem(Long slotId, String title, double price, String addedAt) {
        this.slotId = slotId;
        this.title = title;
        this.price = price;
        this.quantity = 1;
        this.addedAt = addedAt;
    }

    public CartItem(Long slotId, String title, double price, String category, String startDateTime, Integer availableSeats, String status, String addedAt) {
        this.slotId = slotId;
        this.title = title;
        this.price = price;
        this.category = category;
        this.startDateTime = startDateTime;
        this.availableSeats = availableSeats;
        this.status = status;
        this.quantity = 1;
        this.addedAt = addedAt;
    }

    public Long getSlotId() {
        return slotId;
    }

    public String getTitle() {
        return title;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getAddedAt() {
        return addedAt;
    }
}
