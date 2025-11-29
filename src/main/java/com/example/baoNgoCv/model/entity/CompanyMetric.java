package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "company")
public class CompanyMetric {
    private static final int WEIGHT_OPEN_JOB = 10;
    private static final int WEIGHT_INTERVIEW = 5;
    private static final int WEIGHT_FOLLOWER = 1;
    @Id
    @Column(name = "company_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "company_id")
    private Company company;

    /* ---------- Các chỉ số (Logic của bạn) ---------- */

    @Column(name = "open_job_count", nullable = false)
    @Builder.Default
    private Integer openJobCount = 0;

    @Column(name = "total_interview_count", nullable = false)
    private Integer totalInterviewCount = 0;

    @Column(name = "follower_count", nullable = false)
    @Builder.Default
    private Integer followerCount = 0;


    /* ---------- Helper Methods để tăng/giảm số liệu ---------- */

    public void incOpenJob() {
        this.openJobCount = (openJobCount == null ? 1 : openJobCount + 1);
    }

    public void decOpenJob() {
        if (openJobCount != null && openJobCount > 0) this.openJobCount -= 1;
    }

    /**
     * [UPDATED] Tăng tổng số cuộc phỏng vấn theo một số lượng nhất định.
     * @param amount Số lượng phỏng vấn cần tăng.
     */
    public void incrementTotalInterviews(int amount) {
        if (amount <= 0) return;
        this.totalInterviewCount = (this.totalInterviewCount == null ? 0 : this.totalInterviewCount) + amount;
    }

    public void incFollower() {
        this.followerCount = (followerCount == null ? 1 : followerCount + 1);
    }

    public void decFollower() {
        if (followerCount != null && followerCount > 0) this.followerCount -= 1;
    }

    @Transient
    public Integer getCompanyScore() {
        int openJobs = (openJobCount == null) ? 0 : openJobCount;
        int interviews = (totalInterviewCount == null) ? 0 : totalInterviewCount;
        int followers = (followerCount == null) ? 0 : followerCount;

        // Công thức: (Job * 10) + (Interview * 5) + (Follower * 1)
        return (openJobs * WEIGHT_OPEN_JOB)
                + (interviews * WEIGHT_INTERVIEW)
                + (followers * WEIGHT_FOLLOWER);
    } public static CompanyMetric createDefault(Company company) {
        return CompanyMetric.builder()
                .company(company)
                .openJobCount(0)
                .totalInterviewCount(0)
                .followerCount(0)
                .build();
    }
}
