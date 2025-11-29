package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.applicant.GetJobApplicantDetailResponse;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicationDetailResponse;
import com.example.baoNgoCv.model.dto.company.ApplicantFilterRequest;
import com.example.baoNgoCv.model.dto.company.ApplicantViewingDto;
import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.ApplicationStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApplicantService {


    List<Applicant> getApplicantByUser(User user);

    void approveApplicant(Long applicantId);

    void rejectApplicant(Long applicantId);

    boolean existsById(Long applicantId);

    User getUserByApplicanId(Long applicantId);

    Optional<Applicant> findByUserAndJobPosting(User user, JobPosting jobPosting);

    List<String> getAvailablePositionsForCompany(Long companyId);

    Page<Applicant> getFilteredApplicants(Long companyId, String keyword, String position, String status, Pageable pageable);


    @Transactional
    void deleteAllApplicantsByUserPermanently(Long userId);

    void updateStatus(Long applicantId, ApplicationStatus newStatus);

    GetJobApplicantDetailResponse getApplicantDetail(Long id);

    void scheduleInterviewAndSendEmail(Long applicantId,
                                       String subject,
                                       String content
    );


    GetMyApplicationDetailResponse getApplicantDetailForJobseeker(User currentUser, Long applicantId);

    void saveReview(Long applicantId, Double score, String note);

    ApplicantViewingDto getApplicantViewingData(Company company, ApplicantFilterRequest filter);

    // [NEW] Bulk action methods
    void bulkShortlist(List<Long> applicantIds);

    void bulkReject(List<Long> applicantIds);

    void bulkScheduleInterview(List<Long> applicantIds, String subject, String content);

    void withdrawApplication(Long applicantId, Long userId);

    List<Applicant> getApplicantsByJobPostingId(Long jobPostingId);

}