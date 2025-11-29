package com.example.baoNgoCv.jpa.projection.company;

import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface CompanyJobManagementProjection {

    // Basic fields - getter names phải match với alias trong query
    Long getId();

    String getTitle();

    String getJobType();           // Native query trả về String, không phải enum

    String getLocation();

    String getSalaryRange();

    String getExperience();

    String getIndustry();

    LocalDate getApplicationDeadline();

    String getStatus();

    @Value("#{target.received_count}")
    Integer getApplicantCount();

    @Value("#{target.target_hires}")
    Integer getMaxApplicants();

    String getCompanyName();

    // Pipe-delimited strings
    String getDescriptionsJson();

    String getRequirementsJson();

    String getBenefitsJson();

    Integer getEditCount();

    
    // Default methods để parse pipe-delimited strings
    default List<String> getDescriptions() {
        return parsePipeDelimited(getDescriptionsJson());
    }

    default List<String> getRequirements() {
        return parsePipeDelimited(getRequirementsJson());
    }

    default List<String> getBenefits() {
        return parsePipeDelimited(getBenefitsJson());
    }

    // Helper method
    private List<String> parsePipeDelimited(String data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.split("\\|"));
    }
}
