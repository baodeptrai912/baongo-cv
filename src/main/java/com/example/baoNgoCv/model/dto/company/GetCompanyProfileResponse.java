package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.jpa.projection.company.CompanyProfileProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetCompanyProfileResponse {
    private CompanyProfileProjection company;

    public String getCreatedAtString() {
        if (company.getCreatedAt() != null) {
            return company.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }

    public String getUpdatedAtString() {
        if (company.getUpdatedAt() != null) {
            return company.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        return "";
    }
}
