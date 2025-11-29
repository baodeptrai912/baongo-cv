package com.example.baoNgoCv.model.valueObject;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
@Getter
@AllArgsConstructor
@Builder

public class JobMetric {

    @Column(name = "view_count", nullable = false)
    private Integer viewCount ;

    @Column(name = "save_count", nullable = false)
    private Integer saveCount ;

    // 1. Số lượng hồ sơ ĐÃ NHẬN (Đếm số người nộp) -> Thể hiện độ Hot
    @Column(name = "received_count", nullable = false)
    private Integer receivedCount ;

    // 2. Số lượng ĐÃ TUYỂN/DUYỆT (Approved) -> Thể hiện tiến độ
    @Column(name = "hired_count", nullable = false)
    private Integer hiredCount ;

    /* ---------- Trending result ---------- */
    @Column(name = "trending_score", nullable = false, precision = 10, scale = 2)
    private BigDecimal trendingScore ;


    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;


    public JobMetric() {
        // Khởi tạo tất cả các giá trị về 0/mặc định
        this.viewCount = 0;
        this.saveCount = 0;
        this.receivedCount = 0;
        this.hiredCount = 0;
        this.trendingScore = BigDecimal.ZERO;
        this.lastCalculatedAt = null; // Hoặc LocalDateTime.now() tùy vào ý định
    }

    // --- Logic Tăng/Giảm ---

    public void incView() {
        this.viewCount = (viewCount == null ? 1 : viewCount + 1);
    }

    public void incSave() {
        this.saveCount = (saveCount == null ? 1 : saveCount + 1);
    }

    public void decSave() {
        if (saveCount != null && saveCount > 0) this.saveCount -= 1;
    }

    // Tăng khi có người Nộp đơn
    public void incReceived() {
        this.receivedCount = (receivedCount == null ? 1 : receivedCount + 1);
    }

    // Tăng khi HR bấm Duyệt đơn (Approve)
    public void incHired() {
        this.hiredCount = (hiredCount == null ? 1 : hiredCount + 1);
    }

    // Giảm khi HR hủy duyệt
    public void decHired() {
        if (hiredCount != null && hiredCount > 0) this.hiredCount -= 1;
    }

    /**
     * === HÀM NGHIỆP VỤ MỚI ===
     * Cập nhật điểm trending và thời gian tính toán cùng một lúc.
     * Đảm bảo tính nhất quán dữ liệu (Consistency).
     *
     * @param rawTotalScore Điểm số thô vừa tính toán được
     */
    public void updateTrendingScore(long rawTotalScore) {
        this.trendingScore = BigDecimal.valueOf(rawTotalScore);
        this.lastCalculatedAt = LocalDateTime.now();
    }
}