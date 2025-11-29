package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class JobSaved {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private JobPosting jobPosting;

    @CreatedDate
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public static JobSaved create(JobPosting jobPosting, User user) {
        return JobSaved.builder()
                .jobPosting(jobPosting)
                .user(user)
                .build();
    }

    public static JobSaved createAt(JobPosting jobPosting, User user, LocalDateTime savedAt) {
        return JobSaved.builder()
                .jobPosting(jobPosting)
                .user(user)
                .savedAt(savedAt)
                .build();
    }
}
