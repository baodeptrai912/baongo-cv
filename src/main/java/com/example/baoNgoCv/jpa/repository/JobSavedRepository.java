package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.enums.JobType;
import com.example.baoNgoCv.model.enums.LocationType;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.JobSaved;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.projection.jobPosting.SavedJobProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobSavedRepository extends JpaRepository<JobSaved, Long> {


    Optional<JobSaved> findByJobPostingAndUser(JobPosting jobPosting, User currentUser);

    Page<JobSaved> findByUser(User user, Pageable pageable);

    List<JobSaved> findByJobPosting(JobPosting jobPosting);

    @Query("SELECT js FROM JobSaved js WHERE js.user = :user " +
            "AND (:location IS NULL OR js.jobPosting.location = :location) " +
            "AND (:jobType IS NULL OR js.jobPosting.jobType = :jobType)")
    Page<JobSaved> findByUserWithFilters(@Param("user") User user,
                                         @Param("location") String location,
                                         @Param("jobType") String jobType,
                                         Pageable pageable);

    @Query("SELECT DISTINCT jp.location FROM JobSaved js JOIN js.jobPosting jp WHERE js.user = :user AND jp.location IS NOT NULL ORDER BY jp.location")
    List<LocationType> findDistinctLocationsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT jp.jobType FROM JobSaved js JOIN js.jobPosting jp WHERE js.user = :user AND jp.jobType IS NOT NULL ORDER BY jp.jobType")
    List<JobType> findDistinctJobTypesByUser(@Param("user") User user);

    boolean existsByJobPostingAndUser(JobPosting jobPosting, User user);

    @Query(value = "SELECT " +
            "js.savedAt AS savedAt, " +
            "jp.id AS id, " +
            "jp.title AS title, " +
            "jp.location AS location, " +
            "jp.jobType AS jobType, " +
            "jp.salaryRange AS salaryRange, " +
            "jp.postedDate AS postedDate, " +
            "jp.applicationDeadline AS applicationDeadline, " +
            "jp.status AS status, " +
            "jp.experience AS experience, " +
            "jp.industry AS industry, " +
            "jp.targetHires AS maxApplicants, " +
            "SIZE(jp.applicants) AS currentApplicants, " +
            "c.id AS companyId, " +
            "c.name AS companyName, " +
            "c.companyLogo AS companyLogo, " +

            // From Applicant (LEFT JOIN)
            "(CASE WHEN app.id IS NOT NULL THEN TRUE ELSE FALSE END) AS hasApplied, " +
            "ash.status AS applicationStatus, " +
            "app.applicationDate AS applicationDate " +

            "FROM JobSaved js " +
            "JOIN js.jobPosting jp " +
            "JOIN jp.company c " +
            "LEFT JOIN Applicant app ON app.jobPosting = jp AND app.user = js.user " +
            "LEFT JOIN ApplicationStatusHistory ash ON ash.applicant = app AND ash.isCurrent = true " +
            "WHERE js.user.id = :userId "
    )
    Page<SavedJobProjection> findSavedJobsProjectionByUserId(
            @Param("userId") Long userId, Pageable pageable);

    @Query("SELECT j.id as jobId, d as description " +
            "FROM JobPosting j JOIN j.descriptions d " +
            "WHERE j.id IN :jobIds")
    List<JobDescriptionProjection> findDescriptionsByJobIds(@Param("jobIds") List<Long> jobIds);

    @Query("SELECT j.id as jobId, r as requirement " +
            "FROM JobPosting j JOIN j.requirements r " +
            "WHERE j.id IN :jobIds")
    List<JobRequirementProjection> findRequirementsByJobIds(@Param("jobIds") List<Long> jobIds);

    @Query("SELECT j.id as jobId, b as benefit " +
            "FROM JobPosting j JOIN j.benefits b " +
            "WHERE j.id IN :jobIds")
    List<JobBenefitProjection> findBenefitsByJobIds(@Param("jobIds") List<Long> jobIds);

    interface JobDescriptionProjection {
        Long getJobId();
        String getDescription();
    }

    interface JobRequirementProjection {
        Long getJobId();
        String getRequirement();
    }

    interface JobBenefitProjection {
        Long getJobId();
        String getBenefit();
    }

    @Query("SELECT js FROM JobSaved js WHERE js.jobPosting.id = :jobPostingId")
    List<JobSaved> findByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Query("SELECT js FROM JobSaved js WHERE js.jobPosting.id IN :jobIds")
    List<JobSaved> findJobSavedByJobIds(@Param("jobIds") List<Long> jobIds);
}