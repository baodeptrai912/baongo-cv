package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.service.utilityService.FileService;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetProfileUpdateResponse {
    private Long id;
    private String name;
    private String description;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String companyLogoUrl;
    private String companySize;
    private String industry;

    public static GetProfileUpdateResponse fromEntity(Company company, FileService fileService) {

        return GetProfileUpdateResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .location(company.getLocation())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .website(company.getWebsite())
                .companyLogoUrl(company.getCompanyLogo())
                .companySize(company.getCompanySize() != null ?
                        company.getCompanySize().toString() : null)
                .industry(company.getIndustry() != null ?
                        company.getIndustry().name() : null)
                .build();
    }
}