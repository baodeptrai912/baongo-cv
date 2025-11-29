package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.enums.IndustryType;
import com.example.baoNgoCv.model.entity.Company;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PutInformationResponse {

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("company_size")
    private Integer companySize;

    private String description;

    private IndustryType industry;

    public static PutInformationResponse from(Company company) {
        return PutInformationResponse.builder()
                .avatarUrl(company.getCompanyLogo())
                .companySize(company.getCompanySize())
                .description(company.getDescription())
                .industry(company.getIndustry())
                .build();
    }

    // ✅ THÊM METHOD NÀY
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("avatar_url", this.avatarUrl);
        map.put("company_size", this.companySize);
        map.put("description", this.description);
        map.put("industry", this.industry);
        return map;
    }
}
