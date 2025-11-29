package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse;
import com.example.baoNgoCv.jpa.projection.applicant.ApplicantInfoProjection;
import com.example.baoNgoCv.model.dto.company.ApplicantViewingDto;
import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.projection.applicant.MyApplicantStatusHistory;
import com.example.baoNgoCv.model.enums.ApplicationStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ApplicantRepository extends JpaRepository<Applicant, Long> {

    List<Applicant> findByUser(User user);

    @Transactional
    void deleteByJobPosting(JobPosting jobPosting);

    List<Applicant> findByJobPostingId(Long id);

    @Query("SELECT a.user FROM Applicant a WHERE a.id = :applicantId")
    User findUserByApplicantId(@Param("applicantId") Long applicantId);

    @Query("SELECT a.jobPosting FROM Applicant a WHERE a.id = :applicantId")
    JobPosting findJobPostingByApplicantId(Long applicantId);

    @Query("""
                SELECT a
                FROM Applicant a
                WHERE a.user.id = :userId
                  AND a.jobPosting.id = :jobPostingId
            """)
    Optional<Applicant> findExistingApplication(@Param("userId") Long userId, @Param("jobPostingId") Long jobPostingId);

    @Query("SELECT COUNT(a) > 0 FROM Applicant a WHERE a.user.id = :userId AND a.jobPosting.id = :jobPostingId")
    boolean existsByUserIdAndJobPostingId(@Param("userId") Long userId, @Param("jobPostingId") Long jobPostingId);

    List<Applicant> findByJobPosting(JobPosting jobPosting);

    @Modifying
    @Query(value = "DELETE FROM applicant WHERE job_posting_id = :jobPostingId", nativeQuery = true)
    void hardDeleteByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Query(value = "SELECT a.* FROM applicant a " + "JOIN job_posting jp ON a.job_posting_id = jp.id " + "WHERE jp.company_id = :companyId " + "AND (:keyword IS NULL OR :keyword = '' OR " + "     LOWER(a.user_id IN (SELECT u.id FROM user u WHERE LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))) OR " + "     LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "AND (:position IS NULL OR :position = '' OR jp.title = :position) " + "AND (:status IS NULL OR a.status = :status)", // Xóa ORDER BY
            countQuery = "SELECT COUNT(*) FROM applicant a " + "JOIN job_posting jp ON a.job_posting_id = jp.id " + "WHERE jp.company_id = :companyId " + "AND (:keyword IS NULL OR :keyword = '' OR " + "     LOWER(a.user_id IN (SELECT u.id FROM user u WHERE LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')))) OR " + "     LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) " + "AND (:position IS NULL OR :position = '' OR jp.title = :position) " + "AND (:status IS NULL OR a.status = :status)", nativeQuery = true)
    Page<Applicant> findByCompanyIdWithFilters(@Param("companyId") Long companyId, @Param("keyword") String keyword, @Param("position") String position, @Param("status") String status, Pageable pageable);

    @Query("SELECT a FROM Applicant a " + "WHERE a.jobPosting.company.id = :companyId " + "AND a.deletedAt IS NULL " + "ORDER BY a.applicationDate DESC")
    Page<Applicant> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM applicant WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserIdHard(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM application_status_history WHERE applicant_id IN " + "(SELECT id FROM applicant WHERE user_id = :userId)", nativeQuery = true)
    void hardDeleteApplicationStatusHistoryByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM application_review WHERE applicant_id IN " + "(SELECT id FROM applicant WHERE user_id = :userId)", nativeQuery = true)
    void hardDeleteApplicationReviewByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM notification WHERE applicant_id IN " + "(SELECT id FROM applicant WHERE user_id = :userId)", nativeQuery = true)
    void hardDeleteNotificationsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM applicant WHERE user_id = :userId", nativeQuery = true)
    void hardDeleteApplicantsByUserId(@Param("userId") Long userId); // Thay vì deleteByUserIdHard

    @Query("""
            SELECT new com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse$ApplicantCard(
                a.id,
                a.applicationDate,
                a.resume,
                a.jobPosting.id,
                a.jobPosting.title,
                CAST(a.jobPosting.location AS string),
                CAST(a.jobPosting.salaryRange AS string),
                a.jobPosting.company.id,
                a.jobPosting.company.name,
                a.jobPosting.company.companyLogo,
                CAST(csh.status AS string),
                a.review.id
            )
            FROM Applicant a
            JOIN a.jobPosting jp
            JOIN jp.company c
            JOIN a.statusHistory csh ON csh.isCurrent = true
            LEFT JOIN a.review r 
            WHERE a.user = :user AND a.deletedAt IS NULL
            ORDER BY a.applicationDate DESC
            """)
    List<GetMyApplicantResponse.ApplicantCard> findBasicCards(@Param("user") User user);

    @Query("""
                  SELECT new com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse$StatusHistory(
                      sh.applicant.id,
                      CAST(sh.status AS string),
                      sh.statusDate,
                      "building:))",
                                  sh.isCurrent
                  )
                  FROM ApplicationStatusHistory sh
                  WHERE sh.applicant.id IN :applicantIds
            ORDER BY sh.statusDate DESC
            """)
    List<GetMyApplicantResponse.StatusHistory> findHistoriesByApplicantIds(@Param("applicantIds") List<Long> applicantIds);

    @Query("SELECT a.id FROM Applicant a WHERE a.user.id = :userId AND a.jobPosting.id = :jobPostingId")
    Optional<Long> findApplicationIdByUserIdAndJobPostingId(@Param("userId") Long userId, @Param("jobPostingId") Long jobPostingId);

    @Query("""
                SELECT u.id as userId, u.contactInfo.email as userEmail, u.personalInfo.fullName as userFullName, u.username as username
                FROM Applicant a
                JOIN a.user u
                WHERE a.jobPosting.id = :jobId
            """)
    List<ApplicantInfoProjection> findApplicantInfoByJobId(@Param("jobId") Long jobId);

    @Query("""
                SELECT a 
                FROM Applicant a            
                JOIN FETCH a.jobPosting j
                JOIN FETCH j.company c
                LEFT JOIN FETCH a.statusHistory sh            
                LEFT JOIN FETCH a.interviewSchedule i           
                WHERE a.id = :id AND a.user.id = :userId
            """)
    Optional<Applicant> findDetailForJobSeeker(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT a FROM Applicant a " +
            "JOIN FETCH a.user u " +
            "JOIN a.statusHistory sh " +
            "WHERE a.jobPosting.id = :jobPostingId " +
            "AND sh.isCurrent = true " +
            "AND (:status IS NULL OR sh.status = :status)")
    List<Applicant> findByJobPostingIdAndStatus(
            @Param("jobPostingId") Long jobPostingId,
            @Param("status") ApplicationStatus status
    );

    @Query("""
    SELECT NEW com.example.baoNgoCv.model.dto.company.ApplicantViewingDto$StatusCount(
        sh.status, 
        COUNT(a.id)
    )
    FROM Applicant a JOIN a.statusHistory sh
    WHERE a.jobPosting.id = :jobPostingId AND sh.isCurrent = true
    GROUP BY sh.status
""")
    List<ApplicantViewingDto.StatusCount> countApplicantsByStatusForJob(@Param("jobPostingId") Long jobPostingId);

    /**
     * Đếm số lượng ứng viên cho mỗi Job ID thuộc về một công ty.
     * Sử dụng một record (Java 16+) để đóng gói kết quả trả về một cách an toàn.
     */
    @Query("""
        SELECT jp.id as jobId, COUNT(a.id) as applicantCount
        FROM Applicant a JOIN a.jobPosting jp
        WHERE jp.company.id = :companyId
        GROUP BY jp.id
    """)
    List<JobApplicantCount> countApplicantsByJobForCompany(@Param("companyId") Long companyId);

    // Interface-based Projection để nhận kết quả từ query trên
    interface JobApplicantCount {
        Long getJobId();
        Long getApplicantCount();
    }
}
