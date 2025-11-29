package com.example.baoNgoCv.model.dto.company;

import org.springframework.data.domain.Page;
import java.util.List;

public record GetCompaniesResponse(
        Page<CompanyListDTO> companies,
        List<String> availableIndustries,
        List<String> availableLocations,
        String pageTitle,
        String currentSortType,
        int totalResults
) {

    public record CompanyListDTO(
            Long id,
            String name,
            String logoUrl,
            String location,
            String industry,

            Integer openJobs,
            Integer activeInterviews,
            String followersCount, // "1.2k"

            boolean isFollowed
    ) {}
}
