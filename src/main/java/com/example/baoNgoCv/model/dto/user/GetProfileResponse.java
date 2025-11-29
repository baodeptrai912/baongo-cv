package com.example.baoNgoCv.model.dto.user;
import com.example.baoNgoCv.model.valueObject.SocialLink;
import com.example.baoNgoCv.model.enums.Skill;
import com.example.baoNgoCv.jpa.projection.user.BasicProfileResponse;
import com.example.baoNgoCv.jpa.projection.user.EducationDTO;

import java.util.List;
import java.util.Set;

public record GetProfileResponse(
        BasicProfileResponse profile,
        List<JobExperienceDTO> jobExperiences,
        List<EducationDTO> educations,
        Set<Skill> skills,
        Set<SocialLink> socialLinks,
        Long currentUserId
) {

    public Boolean isViewingOwnProfile() {
        return currentUserId != null && currentUserId.equals(profile.id());
    }

    public Boolean isProfilePrivate() {
        return !profile.isProfilePublic() && !isViewingOwnProfile();
    }

    public Boolean isViewingOthersProfile() {
        return !isViewingOwnProfile();
    }
}