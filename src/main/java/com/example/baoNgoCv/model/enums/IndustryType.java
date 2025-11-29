package com.example.baoNgoCv.model.enums;

public enum IndustryType {
    INFORMATION_TECHNOLOGY("Information Technology"),
    FINANCE("Finance"),
    HEALTHCARE("Healthcare"),
    EDUCATION("Education"),
    MANUFACTURING("Manufacturing"),
    RETAIL("Retail"),
    CONSTRUCTION("Construction"),
    TELECOMMUNICATIONS("Telecommunications"),
    HOSPITALITY("Hospitality"),
    ENERGY("Energy"),
    TRANSPORTATION_LOGISTICS("Transportation and Logistics"),
    LEGAL("Legal"),
    REAL_ESTATE("Real Estate"),
    MARKETING_ADVERTISING("Marketing and Advertising"),
    MEDIA_ENTERTAINMENT("Media and Entertainment"),
    AEROSPACE_DEFENSE("Aerospace and Defense"),
    AGRICULTURE("Agriculture"),
    NONPROFIT("Nonprofit"),
    FASHION_APPAREL("Fashion and Apparel"),
    TOURISM("Tourism");

    private final String displayName;

    IndustryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
