package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.jpa.projection.user.BasicProfileResponse;
import com.example.baoNgoCv.jpa.projection.user.EducationDTO;

import java.util.List;

public record GetProfileUpdateResponse(
    BasicProfileResponse basicProfileResponse,
    List<EducationDTO> educations,
    List<JobExperienceDTO> jobExperiences,
    boolean showEducation,
    boolean showJobEx
) {
    public boolean showEducation() {
        return educations != null && !educations.isEmpty();
    }

    public boolean showJobEx() {
        return jobExperiences != null && !jobExperiences.isEmpty();
    }


    public static GetProfileUpdateResponse from(BasicProfileResponse basicProfile,
                                                List<EducationDTO> educations,
                                                List<JobExperienceDTO> jobExperiences) {
        return new GetProfileUpdateResponse(
                basicProfile,
                educations,
                jobExperiences,
                !educations.isEmpty(),
                !jobExperiences.isEmpty()
        );
    }


   }
