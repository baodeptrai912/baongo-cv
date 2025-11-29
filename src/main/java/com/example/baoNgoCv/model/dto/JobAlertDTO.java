package com.example.baoNgoCv.model.dto;

import com.example.baoNgoCv.model.enums.AlertFrequency;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobAlertDTO {

    @NotBlank(message = "Alert name is required")
    @Size(min = 3, max = 100, message = "Alert name must be 3-100 characters")
    private String alertName;

    @Size(max = 255, message = "Keywords must not exceed 255 characters")
    private String keyword;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @Pattern(regexp = "^(0\\+|1\\+|2\\+|3\\+|4\\+|5\\+|Any)?$",
            message = "Invalid experience level")
    private String experience;

    @Pattern(regexp = "^(0-500|500-1000|1000-1500|1500-2000|2000-3000|3000\\+|Negotiable|Any)?$",
            message = "Invalid salary range")
    private String salaryRange;

    @NotNull(message = "Frequency is required")
    private AlertFrequency frequency;

    public JobAlertDTO() {}

    public JobAlertDTO(String alertName, String keyword, String location,
                       String experience, String salaryRange, AlertFrequency frequency) {
        this.alertName = alertName;
        this.keyword = keyword;
        this.location = location;
        this.experience = experience;
        this.salaryRange = salaryRange;
        this.frequency = frequency;
    }

    public String getAlertName() { return alertName; }
    public void setAlertName(String alertName) { this.alertName = alertName; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getSalaryRange() { return salaryRange; }
    public void setSalaryRange(String salaryRange) { this.salaryRange = salaryRange; }

    public AlertFrequency getFrequency() { return frequency; }

    public void setFrequency(AlertFrequency frequency) { this.frequency = frequency; }

    // Utility methods (Optional but recommended)
    @Override
    public String toString() {
        return "JobAlertDTO{" +
                "alertName='" + alertName + '\'' +
                ", keyword='" + keyword + '\'' +
                ", location='" + location + '\'' +
                ", experience='" + experience + '\'' +
                ", salaryRange='" + salaryRange + '\'' +
                ", frequency=" + frequency +
                '}';
    }
}