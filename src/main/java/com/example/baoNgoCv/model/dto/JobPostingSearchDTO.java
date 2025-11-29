package com.example.baoNgoCv.model.dto;

import com.example.baoNgoCv.model.enums.ExperienceLevel;
import com.example.baoNgoCv.model.enums.LocationType;
import com.example.baoNgoCv.model.enums.SalaryRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class JobPostingSearchDTO {
    private Long id;
    private String title;
    private String companyName;
    private String companyLogo;
    private LocationType location;
    private ExperienceLevel experience;
    private SalaryRange salary;



}
