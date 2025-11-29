package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.JobPostingMetricsDTO;
import com.example.baoNgoCv.model.dto.company.GetJobpostingManagingResponse;
import com.example.baoNgoCv.model.dto.company.PutJobPostingRequest;
import com.example.baoNgoCv.model.dto.company.PutJobPostingResponse;
import com.example.baoNgoCv.model.dto.homepage.GetHomePageResponse;
import com.example.baoNgoCv.model.dto.jobposting.*;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.jpa.projection.jobPosting.JobCardProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface JobPostingService {

    GetHomePageResponse getHomePageData();

    Optional<JobPosting> getJobPostingById(long id);

    List<JobPosting> getJobPostingByIndustryId(Long industryId);

    List<JobPosting> getByCompanyId(Long id);

    Page<JobCardProjection> searchJobPostings(GetJobSearchRequest searchRequest);

    void updateJobPostingStatus(Long jobPostingId, JobPostingStatus jobPostingStatus);

    void deleteJobPostingById(Long jobPostingId);

    Page<JobPosting> getCompanyJobPostings(Long companyId, Pageable pageable);

    JobPostingMetricsDTO calculateJobPostingMetrics(Long companyId);

    void deleteJobPosting(Long jobPostingId);

    PostJobResponse publishJobPosting(PostJobRequest createRequest, Long id);

    GetJobResponse getPostAJobPage(Authentication auth);

    GetJobpostingManagingResponse getJobPostingManagement(Pageable pageable);

    GetJobDetailResponse getJobDetail(Long jobId);

    GetApplyJobResponse getApplyJobData(Long jobPostingId);

    void saveJob(Long jobPostingId);

    void unSaveJob(Long jobPostingId);

    GetJobSaveResponse getSavedJobsPage(GetJobSaveRequest request);

    PutJobPostingResponse updateJobPosting(Long id, PutJobPostingRequest request);
}
