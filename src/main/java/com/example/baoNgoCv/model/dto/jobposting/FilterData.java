package com.example.baoNgoCv.model.dto.jobposting;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterData {
    private String title;
    private String location;
    private String experience;
    private String salary;
    private Boolean isFiltering;
    private Integer totalFilteredResults;
}