package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@ToString(exclude = "applicant")
@EntityListeners(AuditingEntityListener.class)
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Applicant applicant;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "status_date", nullable = false)
    private LocalDateTime statusDate;


    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = false;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;



}
