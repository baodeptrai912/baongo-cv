package com.example.baoNgoCv.jpa.projection.company;

import com.example.baoNgoCv.model.entity.Company;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDTO {
    private Long id;
    private String name;

    // 🎯 Static Factory Method cho Job Listing
    public static CompanyDTO forJobpostingManagingListing(Company company) {
        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .build();
    }
}
