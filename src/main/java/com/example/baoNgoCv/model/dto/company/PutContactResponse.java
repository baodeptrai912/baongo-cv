package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.entity.Company;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PutContactResponse {

    private Long companyId;
    private String companyName;
    private String location;
    private String website;
    private String contactEmail;
    private String contactPhone;

    public static PutContactResponse from(Company company) {
        return PutContactResponse.builder()
                .companyId(company.getId())
                .companyName(company.getName())
                .location(company.getLocation())
                .website(company.getWebsite())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("companyId", this.companyId);
        map.put("companyName", this.companyName);
        map.put("location", this.location);
        map.put("website", this.website);
        map.put("contactEmail", this.contactEmail);
        map.put("contactPhone", this.contactPhone);
        return map;
    }
}
