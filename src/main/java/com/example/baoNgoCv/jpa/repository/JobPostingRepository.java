package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.enums.IndustryType;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import com.example.baoNgoCv.jpa.projection.jobPosting.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findById(Long id);

    List<JobPosting> findByIndustry(IndustryType industry);

    List<JobPosting> findByCompanyId(Long id);

    Page<JobPosting> findByCompanyId(Long id, Pageable pageable);

    @Transactional
    void deleteById(Long id);

    @Query("SELECT j FROM JobPosting j WHERE j.title LIKE %:keyword%")
    List<JobPosting> findByTitle(@Param("keyword") String keyword);

    @Query("SELECT j FROM JobPosting j WHERE " +
            "j.company.id = :companyId " +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:experience IS NULL OR :experience = '' OR j.experience = :experience) " +
            "AND (:salaryRange IS NULL OR :salaryRange = '' OR j.salaryRange = :salaryRange)")
    Page<JobPosting> findCompanyJobPostingsByCriteria(@Param("companyId") Long companyId,
                                                      @Param("keyword") String keyword,
                                                      @Param("location") String location,
                                                      @Param("experience") String experience,
                                                      @Param("salaryRange") String salaryRange,
                                                      Pageable pageable);

    // Method mới với parameter name phù hợp với service interface
    @Query("SELECT j FROM JobPosting j WHERE " +
            "j.company.id = :companyId " +
            "AND (:title IS NULL OR :title = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:experience IS NULL OR :experience = '' OR j.experience = :experience) " +
            "AND (:salary IS NULL OR :salary = '' OR j.salaryRange = :salary)")
    Page<JobPosting> findCompanyJobPostingsByTitle(@Param("companyId") Long companyId,
                                                   @Param("title") String title,
                                                   @Param("location") String location,
                                                   @Param("experience") String experience,
                                                   @Param("salary") String salary,
                                                   Pageable pageable);


    @Modifying
    @Transactional
    @Query(value = """
            DELETE ash FROM application_status_history ash
            INNER JOIN applicant a ON ash.applicant_id = a.id 
            WHERE a.job_posting_id = :jobPostingId
            """, nativeQuery = true)
    void hardDeleteApplicationStatusHistoryByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE n FROM notification n 
            INNER JOIN applicant a ON n.applicant_id = a.id 
            WHERE a.job_posting_id = :jobPostingId
            """, nativeQuery = true)
    void hardDeleteNotificationsByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM applicant 
            WHERE job_posting_id = :jobPostingId
            """, nativeQuery = true)
    void hardDeleteApplicantsByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM requirements 
            WHERE job_posting_id = :jobPostingId
            """, nativeQuery = true)
    void hardDeleteRequirementsByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM job_posting 
            WHERE id = :jobPostingId
            """, nativeQuery = true)
    void hardDeleteJobPosting(@Param("jobPostingId") Long jobPostingId);

    @Modifying
    @Query(value = "DELETE FROM application_review " +
            "WHERE applicant_id IN " +
            "(SELECT id FROM applicant WHERE job_posting_id = :jobPostingId)",
            nativeQuery = true)
    void hardDeleteApplicationReviewsByJobPostingId(@Param("jobPostingId") Long jobPostingId);


    @Query("SELECT j FROM JobPosting j " +
            "WHERE j.company.id = :companyId " +
            "AND (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:experience IS NULL OR j.experience = :experience) " +
            "AND (:salary IS NULL OR j.salaryRange = :salary)")
    Page<JobPosting> findJobPostingsByCompanyWithFilters(
            @Param("companyId") Long companyId,
            @Param("title") String title,
            @Param("location") String location,
            @Param("experience") String experience,
            @Param("salary") String salary,
            Pageable pageable
    );

    Page<JobPosting> findAllByOrderByPostedDateDesc(Pageable pageable);

    @Query("SELECT j FROM JobPosting j ORDER BY j.jobMetric.trendingScore DESC")
    Page<JobPosting> findAllByOrderByTrendingScoreDesc(Pageable pageable);



    @Query("SELECT COUNT(j) FROM JobPosting j WHERE j.status = 'OPEN'")
    Integer countActiveJobs();


    @Query("""
            SELECT j.id as id, 
                   j.title as title,
                   j.location as location,
                   j.jobType as jobType, 
                   j.salaryRange as salaryRange,
                   j.experience as experience,
                   j.industry as industry,
                   j.status as status,
                   j.applicationDeadline as applicationDeadline,
                   j.targetHires as maxApplicants,
                   j.postedDate as postedDate,
                   j.company.id as companyId,
                   c.name as companyName,
                   c.companyLogo as companyLogo,
                   COALESCE(j.jobMetric.hiredCount, 0L) as applicantCount,
                   a.id as applicationId,
                   ash.status as applicationStatus,
                   a.deletedAt as applicationDeletedAt,
                   CASE WHEN js.id IS NOT NULL THEN true ELSE false END as jobSaved
            FROM JobPosting j 
            LEFT JOIN j.company c
            LEFT JOIN Applicant a ON a.jobPosting = j AND (:userId IS NOT NULL AND a.user.id = :userId)
            LEFT JOIN ApplicationStatusHistory ash ON ash.applicant = a AND ash.isCurrent = true
            LEFT JOIN JobSaved js ON js.jobPosting = j AND (:userId IS NOT NULL AND js.user.id = :userId)
            WHERE j.id = :jobId
            """)
    Optional<JobDetailWithUserStatusProjection> findJobDetailWithUserStatus(
            @Param("jobId") Long jobId,
            @Param("userId") Long userId
    );


    @Query("SELECT d FROM JobPosting j JOIN j.descriptions d WHERE j.id = :jobId")
    List<String> findDescriptionsByJobId(@Param("jobId") Long jobId);

    @Query("SELECT r FROM JobPosting j JOIN j.requirements r WHERE j.id = :jobId")
    List<String> findRequirementsByJobId(@Param("jobId") Long jobId);

    @Query("SELECT b FROM JobPosting j JOIN j.benefits b WHERE j.id = :jobId")
    List<String> findBenefitsByJobId(@Param("jobId") Long jobId);


    @Query("""
                SELECT j.id as id,
                       j.title as title,
                       j.location as location,
                       c.name as companyName,
                       c.companyLogo as companyLogo
                FROM JobPosting j 
                LEFT JOIN j.company c
                WHERE j.industry = :industry 
                AND j.id != :jobId 
                AND j.status = 'OPEN'
                ORDER BY j.postedDate DESC
            """)
    List<RelevantJobProjection> findRelevantJobsProjection(
            @Param("industry") IndustryType industry,
            @Param("jobId") Long jobId,
            Pageable pageable
    );
    @Query("""
            SELECT 
                j.id as jobId,
                j.title as jobTitle,
                j.location as jobLocation,
                j.salaryRange as jobSalaryRange,
                j.experience as jobExperience,
                j.status as jobStatus,
                j.applicationDeadline as jobApplicationDeadline,
                j.targetHires as jobMaxApplicants,
                j.postedDate as jobCreatedAt,           
                c.id as companyId,
                c.name as companyName,
                c.companyLogo as companyLogo,            
                jm.hiredCount as jobApplicantCount,       
                CASE WHEN :userId IS NOT NULL THEN u.id ELSE null END as userId,
                CASE WHEN :userId IS NOT NULL THEN u.personalInfo.fullName ELSE null END as userFullName,
                CASE WHEN :userId IS NOT NULL THEN u.contactInfo.email ELSE null END as userEmail,
                CASE WHEN :userId IS NOT NULL THEN u.contactInfo.phoneNumber ELSE null END as userPhoneNumber,
                CASE WHEN :userId IS NOT NULL THEN a.id ELSE null END as applicantId,
                CASE WHEN :userId IS NOT NULL THEN ash.status ELSE null END as applicantStatus,
                CASE WHEN :userId IS NOT NULL THEN a.applicationDate ELSE null END as applicantAppliedAt,
                CASE WHEN :userId IS NOT NULL THEN a.deletedAt ELSE null END as applicantDeletedAt,
                CASE WHEN :userId IS NOT NULL THEN js.id ELSE null END as jobSavedId
            FROM JobPosting j
            LEFT JOIN j.company c
            LEFT JOIN j.jobMetric jm
            LEFT JOIN Applicant a ON a.jobPosting.id = j.id AND a.user.id = :userId AND :userId IS NOT NULL
            LEFT JOIN ApplicationStatusHistory ash ON ash.applicant = a AND ash.isCurrent = true
            LEFT JOIN User u ON u.id = :userId AND :userId IS NOT NULL
            LEFT JOIN JobSaved js ON js.jobPosting.id = j.id AND js.user.id = :userId AND :userId IS NOT NULL
            WHERE j.id = :jobId
        """)
    Optional<GetApplyJobProjection> getApplyJobProjection(
            @Param("jobId") Long jobId,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT jp.id as jobPostingId, r as requirement
            FROM JobPosting jp
            JOIN jp.requirements r
            WHERE jp.id IN :jobPostingIds
            """)
    List<RequirementProjection> findTop3RequirementsByJobPostingIds(
            @Param("jobPostingIds") List<Long> jobPostingIds);

    interface RequirementProjection {
        Long getJobPostingId();

        String getRequirement();
    }



    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.company.id = :companyId AND jp.status = :status")
    long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") JobPostingStatus status);

    @Query("SELECT COUNT(jp) FROM JobPosting jp WHERE jp.company.id = :companyId AND jp.status = 'OPEN'")
    long countActiveJobPostingsByCompanyId(@Param("companyId") Long companyId);


    @Query(value = """
            SELECT 
                jp.id AS id,
                jp.title AS title,
                GROUP_CONCAT(DISTINCT d.description SEPARATOR '|||') AS descriptions,
                jp.location AS location,
                jp.job_type AS jobType,            
                jp.salary_range AS salaryRange,    
                jp.posted_date AS postedDate,      
                jp.application_deadline AS applicationDeadline, 
                jp.status AS status,
                jp.experience AS experience,
                jp.industry AS industry,
                jp.target_hires AS targetHires,    
                jp.received_count AS receivedCount, 
                GROUP_CONCAT(DISTINCT r.requirement SEPARATOR '|||') AS requirements,
                GROUP_CONCAT(DISTINCT b.benefit SEPARATOR '|||') AS benefits
            FROM job_posting jp
            LEFT JOIN job_posting_descriptions d ON d.job_posting_id = jp.id
            LEFT JOIN job_posting_requirements r ON r.job_posting_id = jp.id
            LEFT JOIN job_posting_benefits b ON b.job_posting_id = jp.id
            WHERE jp.id = :jobPostingId
            GROUP BY jp.id
            """, nativeQuery = true)
    Optional<JobPostingForApplyJobProjection> findJobPostingForApplyJobProjection(@Param("jobPostingId") Long jobPostingId);


    @Query(value = """
SELECT 
    j.id as id,
    j.title as title,
    j.location as location,
    j.job_type as jobType,
    j.salary_range as salaryRange,
    j.experience as experience,
    j.industry as industry,
    j.posted_date as postedDate,
    j.application_deadline as applicationDeadline,
    j.target_hires as maxApplicants,
    c.name as companyName,
    c.company_logo as companyLogo,
    j.received_count as applicantCount,
    j.view_count as viewCount,
    CASE WHEN j.view_count > 100 THEN 1 ELSE 0 END as trending,
    GROUP_CONCAT(r.requirement SEPARATOR '|||') as requirementsString

FROM job_posting j
JOIN company c ON j.company_id = c.id
LEFT JOIN job_posting_requirements r ON j.id = r.job_posting_id

WHERE
    (:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND (:locationsSize = 0 OR j.location IN :locations)
    AND (:experiencesSize = 0 OR j.experience IN :experiences)
    AND (:salaryRangesSize = 0 OR j.salary_range IN :salaryRanges)
    AND (:jobTypesSize = 0 OR j.job_type IN :jobTypes)
    AND (:industriesSize = 0 OR j.industry IN :industries)
    AND (:jobStatus IS NULL OR j.status = :jobStatus)
    AND (:postedAfter IS NULL OR j.posted_date >= :postedAfter)
    AND (:deadlineBefore IS NULL OR j.application_deadline <= :deadlineBefore)

GROUP BY j.id, j.title, j.location, j.job_type, j.salary_range, j.experience,
         j.industry, j.posted_date, j.application_deadline, j.target_hires,
         c.name, c.company_logo, j.received_count, j.view_count
""", nativeQuery = true)

    Page<JobCardProjection> searchJobPostings(@Param("keyword") String keyword,
                                              @Param("locations") List<String> locations,
                                              @Param("locationsSize") int locationsSize,
                                              @Param("experiences") List<String> experiences,
                                              @Param("experiencesSize") int experiencesSize,
                                              @Param("salaryRanges") List<String> salaryRanges,
                                              @Param("salaryRangesSize") int salaryRangesSize,
                                              @Param("jobTypes") List<String> jobTypes,
                                              @Param("jobTypesSize") int jobTypesSize,
                                              @Param("industries") List<String> industries,
                                              @Param("industriesSize") int industriesSize,
                                              @Param("jobStatus") String jobStatus,
                                              @Param("postedAfter") LocalDate postedAfter,
                                              @Param("deadlineBefore") LocalDate deadlineBefore,
                                              Pageable pageable);


    List<JobPosting> findByStatus(JobPostingStatus status);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("UPDATE JobPosting j SET j.jobMetric.viewCount = COALESCE(j.jobMetric.viewCount, 0) + 1 WHERE j.id = :jobId")
    void incrementViewCount(@Param("jobId") Long jobId);




    List<JobPosting> findByStatusAndApplicationDeadline(@Param("status") JobPostingStatus status, @Param("deadline") LocalDate deadline);

    /**
     * [NEW] Finds all job postings that are either CLOSED or EXPIRED and were last updated
     * before a given cutoff date. This is used by a scheduler to clean up old job postings.
     *
     * @param cutoffDate The date and time to compare against.
     * @return A list of old job postings to be deleted.
     */
    @Query("SELECT j FROM JobPosting j WHERE (j.status = com.example.baoNgoCv.model.enums.JobPostingStatus.CLOSED OR j.status = com.example.baoNgoCv.model.enums.JobPostingStatus.EXPIRED) AND j.updatedAt < :cutoffDate")
    List<JobPosting> findOldClosedOrExpiredJobs(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("""
    SELECT new com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO(
        j.id,
        j.title,
        c.name,
        c.companyLogo,
        j.location,
        j.jobType,
        j.salaryRange,
        j.experience,
        j.industry,
        j.postedDate,
        j.applicationDeadline,
        j.jobMetric.receivedCount,
        j.targetHires,
        j.jobMetric.viewCount,
        CASE WHEN j.jobMetric.trendingScore > 50 THEN true ELSE false END,
        CAST(j.jobMetric.trendingScore AS double),
        null
    )
    FROM JobPosting j
    JOIN j.company c
    WHERE c.id = :companyId
""")
    List<JobCardDTO> findJobCardsByCompanyId(@Param("companyId") Long companyId);

}
