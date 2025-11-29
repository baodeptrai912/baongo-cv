package com.example.baoNgoCv.jpa.projection.jobPosting;

public interface UserForApplyJobProjection {

    // Core attributes (assume embedded objects are flattened)
    Long getId();
    String getUsername();
    String getFullName();
    String getEmail();
    String getPhoneNumber();
    String getProfilePicture();

    default boolean hasProfilePicture() {
        return getProfilePicture() != null && !getProfilePicture().trim().isEmpty();
    }
}
