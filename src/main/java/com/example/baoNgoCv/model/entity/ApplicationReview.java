package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class ApplicationReview {

    @Id
    // XÓA @GeneratedValue vì giờ ID không tự tăng nữa mà lấy từ Applicant
    private Long id; // ID này sẽ bằng với applicant.id

    @OneToOne
    @MapsId // <--- QUAN TRỌNG: Báo cho JPA biết "Hãy dùng applicant.id làm id của Review"
    @JoinColumn(name = "applicant_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Applicant applicant;

    @Column(name = "overall_rating")
    private Double rating;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company reviewer;

    @Column(name = "review_comment", length = 5000)
    private String reviewComments;

    @Column(name = "review_date")
    private LocalDateTime reviewDate;

    // =========================================================================
    //  Static Factory Method
    // =========================================================================
    public static ApplicationReview create(Applicant applicant, Double rating, String reviewComments, Company reviewer) {
        ApplicationReview review = new ApplicationReview();
        review.setApplicant(applicant);
        review.setRating(rating);
        review.setReviewComments(reviewComments);
        review.setReviewer(reviewer);
        review.setReviewDate(LocalDateTime.now());
        return review;
    }

    // Update method
    public void update(Double newRating, String newComments) {
        this.setRating(newRating);
        this.setReviewComments(newComments);
        this.setReviewDate(LocalDateTime.now());
    }
}
