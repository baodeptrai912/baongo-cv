package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.enums.AlertFrequency;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class JobAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_name", nullable = false, length = 255)
    private String alertName;

    @Column(length = 500)
    private String keyword;

    @Column(length = 100)
    private String location;

    @Column(length = 10)
    private String experience;

    @Column(name = "salary_range", length = 50)
    private String salaryRange;


    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private AlertFrequency frequency;

    // Quan hệ với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // Quản lý trạng thái
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public JobAlert() {}

    public JobAlert(String alertName, String keyword, String location,
                    String experience, String salaryRange, AlertFrequency frequency, User user) {

        this.alertName = alertName;
        this.keyword = keyword;
        this.location = location;
        this.experience = experience;
        this.salaryRange = salaryRange;
        this.frequency = frequency;
        this.user = user;
    }


}
