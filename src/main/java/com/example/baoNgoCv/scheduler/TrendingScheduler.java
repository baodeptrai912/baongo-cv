package com.example.baoNgoCv.scheduler;

import com.example.baoNgoCv.jpa.repository.JobPostingRepository;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import com.example.baoNgoCv.model.valueObject.JobMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrendingScheduler {

    private final JobPostingRepository jobPostingRepository;

    // --- CẤU HÌNH TRỌNG SỐ (WEIGHTS) ---
    // Bạn có thể điều chỉnh các số này để thay đổi độ quan trọng
    private static final int WEIGHT_VIEW = 1;       // 1 View = 1 điểm
    private static final int WEIGHT_SAVE = 5;       // 1 Save = 5 điểm (User quan tâm)
    private static final int WEIGHT_RECEIVED = 10;  // 1 Đơn nộp = 10 điểm (Quan trọng nhất)

    /**
     * Scheduled Task: Cập nhật điểm Trending định kỳ.
     *
     * initialDelay: Đợi 1 phút sau khi server khởi động mới chạy lần đầu (để server ổn định).
     * fixedRate: Chạy lặp lại mỗi 30 phút (1.800.000 ms).
     */
    @Scheduled(initialDelay = 60000, fixedRate = 1800000)
    @Transactional
    public void updateJobTrendingScores() {
        // 1. Ghi log thông báo bắt đầu quá trình cập nhật
        log.info(">>> Bắt đầu Job: Tính toán lại Trending Score cho các công việc...");

        // 2. Ghi lại thời gian bắt đầu để đo lường hiệu suất của job
        long startTime = System.currentTimeMillis();

        // 3. Lấy danh sách các Job đang ở trạng thái "OPEN" từ repository
        // Chúng ta chỉ tính điểm cho các công việc đang hoạt động để tiết kiệm tài nguyên
        List<JobPosting> activeJobs = jobPostingRepository.findByStatus(JobPostingStatus.OPEN);

        // 4. Kiểm tra nếu không có công việc nào đang mở, ghi log và kết thúc sớm
        if (activeJobs.isEmpty()) {
            log.info(">>> Không có công việc nào đang mở. Kết thúc Job.");
            return;
        }

        // 5. Lặp qua từng công việc đang hoạt động để tính toán và cập nhật điểm trending
        for (JobPosting job : activeJobs) {
            updateScoreForJob(job);
        }

        // 6. Lưu tất cả các đối tượng đã được cập nhật vào cơ sở dữ liệu
        // Nhờ có @Transactional, Hibernate sẽ quản lý và tối ưu các câu lệnh UPDATE (thường bằng batch update)
        jobPostingRepository.saveAll(activeJobs);

        // 7. Tính toán và ghi log thời gian hoàn thành job cùng với số lượng công việc đã được cập nhật
        long duration = System.currentTimeMillis() - startTime;
        log.info(">>> Hoàn thành Job Trending. Đã cập nhật {} công việc trong {} ms.", activeJobs.size(), duration);
    }

    /**
     * Logic tính điểm chi tiết cho từng Job
     */
    private void updateScoreForJob(JobPosting job) {
        JobMetric metric = job.getJobMetric();

        // 1. Kiểm tra null cho các thuộc tính trong JobMetric để đảm bảo an toàn
        // Nếu một giá trị nào đó là null (có thể do dữ liệu cũ), nó sẽ được coi là 0
        int views = metric.getViewCount() != null ? metric.getViewCount() : 0;
        int saves = metric.getSaveCount() != null ? metric.getSaveCount() : 0;
        int received = metric.getReceivedCount() != null ? metric.getReceivedCount() : 0;

        // 2. Tính điểm thô (raw score) dựa trên công thức tổng trọng số (Weighted Sum)
        // Mỗi hành động (view, save, apply) có một trọng số khác nhau để phản ánh mức độ quan trọng
        long rawScore = (long) (views * WEIGHT_VIEW)
                + (long) (saves * WEIGHT_SAVE)
                + (long) (received * WEIGHT_RECEIVED);

        // 3. Gọi phương thức trong đối tượng JobMetric để cập nhật điểm trending mới
        metric.updateTrendingScore(rawScore);
    }


}