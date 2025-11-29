package com.example.baoNgoCv.mapper;

import com.example.baoNgoCv.model.dto.jobposting.GetJobSaveResponse;
import com.example.baoNgoCv.jpa.projection.jobPosting.SavedJobProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobSavedMapper {


    @Mapping(source = "id", target = "jobId")
    @Mapping(source = "applicationDate", target = "appliedAt")
    GetJobSaveResponse.SavedJobsData.SavedJobItem toSavedJobItem(SavedJobProjection projection);


}
