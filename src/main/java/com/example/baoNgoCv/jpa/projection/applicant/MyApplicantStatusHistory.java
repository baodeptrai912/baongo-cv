package com.example.baoNgoCv.jpa.projection.applicant;

import com.example.baoNgoCv.model.enums.ApplicationStatus;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface MyApplicantStatusHistory {
    Long getApplicantId();
    ApplicationStatus getStatus();
    LocalDateTime getStatusDate();
    String getStatusNote();
    Boolean getCurrent();

    // Computed methods cho template
    default String getStatusDisplayName() {
        return StringUtils.capitalize(getStatus().name().toLowerCase());
    }

    default String getFormattedDate() {
        return getStatusDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"));
    }


}