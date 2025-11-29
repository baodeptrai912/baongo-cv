package com.example.baoNgoCv.model.dto.jobposting;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationData {

    private Integer totalPages;
    private Long totalElements;
    private Integer number;
    private Integer size;
    private Integer numberOfElements;

    // Navigation flags
    private Boolean first;
    private Boolean last;
    private Boolean empty;

    // Helper fields cho pagination controls
    private Integer currentPageDisplay;
    private Integer startElement;
    private Integer endElement;
}
