package com.example.baoNgoCv.scheduler;

import com.example.baoNgoCv.event.jobposting.JobPostingExpiredEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingExpiringSoonEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingReminderEvent;
import com.example.baoNgoCv.jpa.repository.JobPostingRepository;
import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.enums.ExpireReason;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import com.example.baoNgoCv.service.domainService.JobPostingServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPostingExpirationScheduler {

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingServiceImpl jobPostingServiceImpl;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Tác vụ này chạy hàng ngày để:
     * 1. Cập nhật các công việc đã hết hạn.
     * 2. Kích hoạt sự kiện nhắc nhở cho các công việc sắp hết hạn.
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Ho_Chi_Minh")
    public void processJobStatus() {
        log.info("🔍 [SCHEDULER] Bắt đầu tác vụ kiểm tra trạng thái công việc...");

        LocalDate today = LocalDate.now();

        // Tối ưu: Thay vì findAll(), hãy tạo query chuyên biệt trong repository
        // Ví dụ: findByStatus(JobPostingStatus.OPEN) để giảm tải
        List<JobPosting> activeJobs = jobPostingRepository.findByStatus(JobPostingStatus.OPEN);

        log.info("Phát hiện {} công việc đang hoạt động để kiểm tra.", activeJobs.size());

        for (JobPosting job : activeJobs) {
            LocalDate deadline = job.getApplicationDeadline();
            if (deadline == null) {
                continue; // Bỏ qua nếu không có deadline
            }

            // Trường hợp 1: Công việc đã hết hạn
            if (deadline.isBefore(today)) {
                handleExpiredJob(job);
                continue; // Chuyển sang job tiếp theo
            }

            // Trường hợp 2: Công việc sắp hết hạn (còn 1 hoặc 2 ngày)
            long daysLeft = ChronoUnit.DAYS.between(today, deadline);
            if (daysLeft > 0 && daysLeft <= 3) {
                handleExpiringSoonJob(job);
            }
        }

        log.info("✅ [SCHEDULER] Hoàn tất tác vụ kiểm tra trạng thái công việc.");
    }

    /**
     * Xử lý một công việc đã hết hạn.
     * Cập nhật trạng thái và bắn sự kiện JobPostingExpiredEvent.
     */
    private void handleExpiredJob(JobPosting job) {
        log.info("Công việc '{}' (ID: {}) đã hết hạn. Cập nhật trạng thái và bắn sự kiện.", job.getTitle(), job.getId());
        jobPostingServiceImpl.updateJobPostingStatus(job.getId(), JobPostingStatus.EXPIRED);

        // Bắn sự kiện để các handler khác (ví dụ: gửi email cho nhà tuyển dụng) xử lý
        eventPublisher.publishEvent(new JobPostingExpiredEvent(
                job.getId(),
                job.getTitle(),
                job.getCompany().getId(),
                job.getCompany().getContactEmail(),
                job.getCompany().getName(),
                ExpireReason.DEADLINE_PASSED
        ));
    }

    /**
     * Xử lý một công việc sắp hết hạn.
     * Thu thập thông tin và bắn sự kiện JobPostingReminderEvent.
     */
    private void handleExpiringSoonJob(JobPosting job) {
        // Lấy danh sách ID của các user đã lưu công việc này
        List<Long> savedUserIds = job.getSavedJobs().stream()
                .map(jobSaved -> jobSaved.getUser().getId())
                .collect(Collectors.toList());

        // Nếu có người dùng đã lưu, bắn sự kiện để gửi thông báo nhắc nhở
        if (!savedUserIds.isEmpty()) {
            log.info("Công việc '{}' (ID: {}) sắp hết hạn. Bắn sự kiện nhắc nhở cho {} người dùng.", job.getTitle(), job.getId(), savedUserIds.size());
            eventPublisher.publishEvent(new JobPostingReminderEvent(
                    job.getId(),
                    job.getTitle(),
                    savedUserIds
            ));
        }
    }

    /**
     * [NEW] Tác vụ này chạy hàng ngày (vào 2 giờ sáng) để gửi email nhắc nhở
     * cho các công ty có bài đăng sắp hết hạn trong vòng 3 ngày tới.
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Ho_Chi_Minh")
    public void remindExpiringJobs() {
        log.info("🔍 [SCHEDULER] Bắt đầu tác vụ nhắc nhở công việc sắp hết hạn...");
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(3);

        // Tìm các công việc đang mở và sẽ hết hạn trong đúng 3 ngày nữa
        List<JobPosting> expiringJobs = jobPostingRepository.findByStatusAndApplicationDeadline(JobPostingStatus.OPEN, reminderDate);

        if (expiringJobs.isEmpty()) {
            log.info("Không có công việc nào sắp hết hạn trong 3 ngày tới.");
            return;
        }

        log.info("Phát hiện {} công việc sắp hết hạn. Gửi sự kiện nhắc nhở...", expiringJobs.size());

        for (JobPosting job : expiringJobs) {
            eventPublisher.publishEvent(new JobPostingExpiringSoonEvent(
                    job.getId(),
                    job.getTitle(),
                    job.getCompany().getId(),
                    job.getCompany().getName(),
                    job.getCompany().getContactEmail()
            ));
        }
        log.info("✅ [SCHEDULER] Hoàn tất tác vụ nhắc nhở.");
    }

    /**
     * [NEW] Tác vụ này chạy hàng ngày (vào 3 giờ sáng) để dọn dẹp các bài đăng đã cũ.
     * Nó sẽ xóa các bài đăng có trạng thái CLOSED hoặc EXPIRED lâu hơn 2 tuần.
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Ho_Chi_Minh")
    public void cleanupOldJobPostings() {
        log.info("🧹 [SCHEDULER] Bắt đầu tác vụ dọn dẹp các công việc cũ...");
        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        // Tìm các công việc đã đóng hoặc hết hạn trước 2 tuần
        List<JobPosting> jobsToDelete = jobPostingRepository.findOldClosedOrExpiredJobs(twoWeeksAgo);

        if (jobsToDelete.isEmpty()) {
            log.info("Không có công việc cũ nào cần dọn dẹp.");
            return;
        }

        log.warn("Phát hiện {} công việc cũ cần xóa. Bắt đầu xóa...", jobsToDelete.size());
        jobPostingRepository.deleteAll(jobsToDelete);
        log.info("✅ [SCHEDULER] Đã xóa thành công {} công việc cũ.", jobsToDelete.size());
    }
}
