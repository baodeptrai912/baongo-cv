package com.example.baoNgoCv.jpa.projection.company;

import com.example.baoNgoCv.model.enums.IndustryType;

import java.time.LocalDateTime;
import java.util.Set;

public interface CompanyProfileProjection {
    Long getId();
    String getName();
    String getCompanyLogo();
    String getDescription();
    String getLocation();
    String getContactEmail();
    String getContactPhone();
    String getWebsite();
    Integer getCompanySize();
    IndustryType getIndustry();
    SubscriptionDetailsProjection getSubscriptionDetails();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Set<Object> getFollowers();
}
