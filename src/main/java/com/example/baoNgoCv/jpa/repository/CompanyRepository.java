package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.jpa.projection.company.*;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.IndustryType;
import com.example.baoNgoCv.model.enums.LocationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Set;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findById(Long id);

    Optional<Company> findByContactEmail(String contactEmail);

    Optional<Company> findByName(String companyName);

    Optional<Company> findByUsername(String username);

    @Query("""
            SELECT f.id
            FROM Company c
            JOIN c.followers f
            WHERE c.id = :companyId
            """)
    Set<Long> findFollowerIdsByCompanyId(@Param("companyId") Long companyId);

    @Query("""
            SELECT new com.example.baoNgoCv.jpa.projection.company.CompanyDetailDTO(
                c.id,
                c.name,
                c.description,
                c.location,
                c.contactEmail,
                c.contactPhone,
                c.website,
                c.companyLogo,
                c.createdAt,
                c.companySize,
                c.industry,
                 CAST(SIZE(c.followers) AS long)
            )
            FROM Company c
            WHERE c.id = :companyId
            """)
    Optional<CompanyDetailDTO> findCompanyDetailById(@Param("companyId") Long companyId);

    @Query("SELECT c.id AS id, c.name AS name, c.companyLogo AS companyLogo FROM Company c WHERE c.id = :companyId")
    Optional<CompanyInfoProjection> findCompanyInfoById(@Param("companyId") Long companyId);

    @Query("""
                SELECT c.id AS id, c.name AS name, c.companyLogo AS companyLogo 
                FROM Company c 
                JOIN JobPosting j ON j.company.id = c.id 
                WHERE j.id = :jobPostingId
            """)
    Optional<CompanyInfoProjection> findCompanyInfoByJobPostingId(@Param("jobPostingId") Long jobPostingId);

    @Query("SELECT c FROM Company c WHERE c.id = :id")
    Optional<CompanyProfileProjection> findCompanyProfileProjectionById(@Param("id") Long id);

    @Query("SELECT c FROM Company c WHERE c.subscriptionDetails.accountTier <> :accountTier AND c.subscriptionDetails.planExpirationDate < :dateTime")
    List<Company> findExpiredPaidSubscriptions(
            @Param("accountTier") AccountTier accountTier,
            @Param("dateTime") LocalDateTime dateTime
    );


    @Query(
            value = """
                    SELECT 
                        jp.id AS id,
                        jp.title AS title,
                        jp.job_type AS jobType,
                        jp.location AS location,
                        jp.salary_range AS salaryRange,
                        jp.experience AS experience,
                        jp.industry AS industry,
                        jp.application_deadline AS applicationDeadline,
                        jp.status AS status,
                        jp.received_count AS received_count,
                        jp.target_hires AS target_hires,
                        jp.edit_count AS editCount,
                        c.name AS companyName,
                        COALESCE(GROUP_CONCAT(DISTINCT jpd.description SEPARATOR '|'), '') AS descriptionsJson,
                        COALESCE(GROUP_CONCAT(DISTINCT jpr.requirement SEPARATOR '|'), '') AS requirementsJson,
                        COALESCE(GROUP_CONCAT(DISTINCT jpb.benefit SEPARATOR '|'), '') AS benefitsJson
                    FROM job_posting jp
                    JOIN company c ON jp.company_id = c.id
                    LEFT JOIN job_posting_descriptions jpd ON jp.id = jpd.job_posting_id
                    LEFT JOIN job_posting_requirements jpr ON jp.id = jpr.job_posting_id
                    LEFT JOIN job_posting_benefits jpb ON jp.id = jpb.job_posting_id
                    WHERE c.id = :companyId
                    GROUP BY jp.id, c.name, jp.title, jp.job_type, jp.location, jp.salary_range,
                             jp.experience, jp.industry, jp.application_deadline, jp.status,
                             jp.received_count, jp.target_hires, jp.edit_count
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT jp.id)
                    FROM job_posting jp
                    WHERE jp.company_id = :companyId
                    """,
            nativeQuery = true
    )
    Page<CompanyJobManagementProjection> findJobPostingsForManagement(
            @Param("companyId") Long companyId,
            Pageable pageable
    );



    @Query(
            value = """
                    SELECT NEW com.example.baoNgoCv.jpa.projection.company.SubscriptionUsageProjection(
                        CAST(    c.subscriptionDetails.jobPostingsThisCycle AS INTEGER),
                        CAST(CASE c.subscriptionDetails.accountTier
                            WHEN 'FREE' THEN 1
                            WHEN 'BASIC' THEN 3
                            WHEN 'PREMIUM' THEN 5
                            ELSE 0
                        END AS integer ),
                       c.subscriptionDetails.accountTier, 
                        CAST(FLOOR(
                              c.subscriptionDetails.jobPostingsThisCycle  * 100.0 /
                            (CASE c.subscriptionDetails.accountTier
                                WHEN 'FREE' THEN 1
                                WHEN 'BASIC' THEN 3
                                WHEN 'PREMIUM' THEN 5
                                ELSE 1
                            END)
                        ) AS INTEGER)
                    )
                    FROM
                        Company c
                    
                    WHERE
                        c.id = :companyId
                    GROUP BY
                        c.id, c.subscriptionDetails.accountTier, c.subscriptionDetails.cycleStartDate
                    """
    )
    Optional<SubscriptionUsageProjection> findActiveSubscriptionUsageByCompanyId(@Param("companyId") Long companyId);

    @Query(
            value = """
                    SELECT NEW com.example.baoNgoCv.jpa.projection.company.JobStatisticsProjection(
                        CAST(COUNT(jp.id) AS INTEGER),
                        CAST(SUM(CASE WHEN jp.status = 'OPEN' THEN 1 ELSE 0 END) AS INTEGER),
                        CAST(SUM(CASE WHEN jp.status = 'CLOSED' THEN 1 ELSE 0 END) AS INTEGER),
                        CAST(COALESCE(SUM(jp.jobMetric.receivedCount), 0) AS INTEGER),
                        CAST(SUM(CASE WHEN jp.status = 'OPEN' AND jp.applicationDeadline 
                            BETWEEN CURRENT_DATE AND :expirationThresholdDate THEN 1 ELSE 0 END) AS int)
                    )
                    FROM JobPosting jp
                    WHERE jp.company.id = :companyId
                    """
    )
    Optional<JobStatisticsProjection> getJobStatisticsByCompanyId(
            @Param("companyId") Long companyId,
            @Param("expirationThresholdDate") LocalDate expirationThresholdDate
    );


    @Query("SELECT c FROM Company c " +
            "LEFT JOIN c.companyMetric m " +
            "WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND ((:industries) IS NULL OR c.industry IN (:industries)) " +
            "AND (:location IS NULL OR c.location = :location)")
    Page<Company> searchCompanies(@Param("keyword") String keyword,
                                  @Param("industries") List<IndustryType> industries,
                                  @Param("location") LocationType location,
                                  Pageable pageable);


    @Query("select c.id from Company c join c.followers u where u.id = :userId")
    Set<Long> findFollowedCompanyIdsByUserId(@Param("userId") Long userId);
}
