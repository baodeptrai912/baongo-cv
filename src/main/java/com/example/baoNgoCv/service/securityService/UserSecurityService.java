package com.example.baoNgoCv.service.securityService;
import com.example.baoNgoCv.jpa.repository.EducationRepository;
import com.example.baoNgoCv.jpa.repository.JobExperienceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component("userSecurityService")
@RequiredArgsConstructor
public class UserSecurityService {

    private final EducationRepository educationRepository;
    private final JobExperienceRepository jobExperienceRepository;

    public boolean isOwnerById(Long educationId, Long userId) {
        return educationRepository.findById(educationId)
                .map(education -> education.getUser().getId().equals(userId))
                .orElse(false);
    }
    public boolean isJobExperienceOwner(Long jobExperienceId, Long userId) {
        return jobExperienceRepository.findById(jobExperienceId)
                .map(jobExperience -> jobExperience.getUser().getId().equals(userId))
                .orElse(false);
    }
}