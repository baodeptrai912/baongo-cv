package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.enums.AlertFrequency;
import com.example.baoNgoCv.model.entity.JobAlert;
import com.example.baoNgoCv.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {  // ✅ Long thay vì Integer

    List<JobAlert> findByUserAndIsActiveTrueOrderByCreatedAtDesc(User user);

    long countByUserAndIsActiveTrue(User user);

    List<JobAlert> findByFrequencyAndIsActiveTrue(AlertFrequency frequency);  // ✅ AlertFrequency thay vì String

    // Additional useful methods
    List<JobAlert> findByUserAndIsActiveTrue(User user);

    List<JobAlert> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndAlertNameAndIsActiveTrue(User user, String alertName);

    // Query methods với @Query annotation (alternative approach)
    @Query("SELECT ja FROM JobAlert ja WHERE ja.user = :user AND ja.isActive = true")
    List<JobAlert> findActiveAlertsByUser(@Param("user") User user);

    @Query("SELECT COUNT(ja) FROM JobAlert ja WHERE ja.user = :user AND ja.isActive = true")
    long countActiveAlertsByUser(@Param("user") User user);

    @Query("SELECT ja FROM JobAlert ja WHERE ja.frequency = :frequency AND ja.isActive = true")
    List<JobAlert> findActiveAlertsByFrequency(@Param("frequency") AlertFrequency frequency);

    Optional<JobAlert> findByIdAndUser(Long id, User user);

}
