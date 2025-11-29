package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.entity.ApplicationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationReviewRepository extends JpaRepository<ApplicationReview, Long> {


    ApplicationReview findByApplicantId(Long applicantId);
}