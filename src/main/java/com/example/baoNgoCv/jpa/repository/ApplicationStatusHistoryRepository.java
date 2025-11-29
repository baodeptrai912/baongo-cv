package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.enums.ApplicationStatus;
import com.example.baoNgoCv.model.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory> findByApplicantIdOrderByStatusDateAsc(Long applicantId);

    Optional<ApplicationStatusHistory> findByApplicantIdAndIsCurrent(Long applicantId, boolean isCurrent);

    @Query("SELECT ash FROM ApplicationStatusHistory ash WHERE ash.applicant.id = :applicantId ORDER BY ash.statusDate DESC")
    List<ApplicationStatusHistory> findByApplicantIdOrderByStatusDateDesc(@Param("applicantId") Long applicantId);

    @Query("SELECT COUNT(ash) FROM ApplicationStatusHistory ash WHERE ash.applicant.id = :applicantId AND ash.status = :status")
    long countByApplicantIdAndStatus(@Param("applicantId") Long applicantId, @Param("status") ApplicationStatus status);
}
