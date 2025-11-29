package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.JobAlertDTO;
import com.example.baoNgoCv.model.dto.JobAlertResponse;
import com.example.baoNgoCv.model.entity.JobAlert;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.repository.JobAlertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobAlertServiceImpl implements JobAlertService {
    private final UserService userService;
    private final JobAlertRepository jobAlertRepository;



    @Override
    public JobAlert save(JobAlert jobAlert) {
        return null;
    }

    @Override
    public JobAlertResponse createJobAlert(JobAlertDTO dto) {
//
//        log.info("Creating job alert: alertName={}, frequency={}", dto.getAlertName(), dto.getFrequency());
//
//        try {
//            User currentUser = userService.getCurrentUser();
//            if (currentUser == null) {
//                log.error("No authenticated user found");
//                throw new RuntimeException("User not authenticated");
//            }
//
//            log.debug("User authenticated: id={}, email={}", currentUser.getId(), currentUser.getEmail());
//
//            long alertCount = jobAlertRepository.countByUserAndIsActiveTrue(currentUser);
//            log.debug("Current alert count: {}/3 for user {}", alertCount, currentUser.getId());
//
//            if (alertCount >= 3) {
//                log.warn("Alert limit exceeded for user {}: {}/3", currentUser.getId(), alertCount);
//                throw new RuntimeException("You can only create up to 3 job alerts");
//            }
//
//            JobAlert jobAlert = jobAlertMapper.toEntity(dto, currentUser);
//            log.debug("JobAlert entity created: {}", jobAlert.getAlertName());
//
//            JobAlert saved = jobAlertRepository.save(jobAlert);
//            log.info("Job alert saved: id={}, name={}", saved.getId(), saved.getAlertName());
//
//            JobAlertResponse response = jobAlertMapper.toResponse(saved);
//            log.info("Job alert creation completed successfully: id={}", response.getId());
//
//            return response;
//
//        } catch (Exception e) {
//            log.error("Failed to create job alert '{}': {}", dto.getAlertName(), e.getMessage(), e);
//            throw e;
//        }
        return null;
    }

    @Override
    public List<JobAlert> getUserJobAlerts(User user) {
        List<JobAlert> entities = jobAlertRepository.findByUserAndIsActiveTrueOrderByCreatedAtDesc(user);

        log.debug("Mapped  entities → responses for user {}", user.getId());
        return entities;
    }

    @Transactional
    public void deleteJobAlert(Long alertId) {

        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("User not authenticated");
        }

        JobAlert jobAlert = jobAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Job alert not found"));

        if (!jobAlert.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You don't have permission to delete this job alert");
        }

        jobAlertRepository.delete(jobAlert);
    }

    @Override
    public JobAlertResponse updateJobAlert(Long id, JobAlertDTO dto) {

        log.info("Updating job alert: id={}, alertName={}", id, dto.getAlertName());

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            log.error("No authenticated user found");
            throw new RuntimeException("User not authenticated");
        }

        log.debug("User authenticated: id={}, email={}", currentUser.getId(), currentUser.getContactInfo().getEmail());

        JobAlert existingAlert = jobAlertRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> {
                    log.warn("Job alert not found or access denied: id={}, userId={}", id, currentUser.getId());
                    return new RuntimeException("Job alert not found or access denied");
                });

        log.debug("Found existing alert: id={}, name={}", existingAlert.getId(), existingAlert.getAlertName());

        // Update entity
        existingAlert.setAlertName(dto.getAlertName());
        existingAlert.setKeyword(dto.getKeyword());
        existingAlert.setLocation(dto.getLocation());
        existingAlert.setExperience(dto.getExperience());
        existingAlert.setSalaryRange(dto.getSalaryRange());
        existingAlert.setFrequency(dto.getFrequency());
        existingAlert.setUpdatedAt(LocalDateTime.now());

        log.debug("Alert entity updated with new data");

        JobAlert savedAlert = jobAlertRepository.save(existingAlert);
        log.info("Job alert updated successfully: id={}, name={}", savedAlert.getId(), savedAlert.getAlertName());

//        JobAlertResponse response = jobAlertMapper.toResponse(savedAlert);
//        log.info("Job alert update completed successfully: id={}", response.getId());

        return null;
    }

}
