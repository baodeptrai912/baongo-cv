package com.example.baoNgoCv.model.dto.jobposting;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class GetJobSaveRequest {

    private Long notiId;
    private Integer page;
    private Integer size;

    public Pageable toPageable() {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 6;
        return PageRequest.of(
                pageNumber,
                pageSize,
                Sort.by(Sort.Direction.DESC, "savedAt")
        );
    }

    public static GetJobSaveRequest withDefaults() {
        return GetJobSaveRequest.builder()
                .page(0)
                .size(6)
                .build();
    }
}
