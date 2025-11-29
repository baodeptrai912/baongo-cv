package com.example.baoNgoCv.model.dto;



public class ProfileVisibilityUpdateDTO {
    private String profileVisibility; // Sẽ nhận giá셔 "PUBLIC" hoặc "PRIVATE"

    public String getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(String profileVisibility) {
        this.profileVisibility = profileVisibility;
    }
}