package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.event.applicant.ApplicantStatusChangedEvent;
import com.example.baoNgoCv.event.applicant.ApplicantWithdrewEvent;
import com.example.baoNgoCv.exception.application.ApplicantStatusTransitionException;
import com.example.baoNgoCv.exception.application.BulkApplicantActionException;
import com.example.baoNgoCv.exception.application.ApplicationNotFoundException;
import com.example.baoNgoCv.model.dto.company.ApplicantFilterRequest;
import com.example.baoNgoCv.model.dto.company.ApplicantViewingDto;
import com.example.baoNgoCv.model.dto.applicant.GetJobApplicantDetailResponse;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicationDetailResponse;
import com.example.baoNgoCv.model.enums.ApplicationStatus;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository;
import com.example.baoNgoCv.model.enums.NotificationType;
import com.example.baoNgoCv.jpa.repository.ApplicationReviewRepository;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository.JobApplicantCount;
import com.example.baoNgoCv.jpa.repository.JobPostingRepository;
import com.example.baoNgoCv.jpa.repository.NotificationRepository;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicantServiceImpl implements ApplicantService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserService userService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final ApplicantRepository applicantRepository;

    private final JobPostingService jobPostingService;

    private final EmailService emailService;

    private final FileService fileService;

    private final JobPostingRepository jobPostingRepository;

    private final NotificationRepository notificationRepository;

    private final NotificationService notificationService;

    private final ApplicationReviewRepository applicationReviewRepository;

    @Override
    public List<Applicant> getApplicantByUser(User user) {
        // Lấy danh sách công việc của người dùng
        List<Applicant> applicant = applicantRepository.findByUser(user);


        return applicant;
    }

    @Override
    @Transactional
    public void approveApplicant(Long applicantId) {
        // 1. Fetch Data
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found"));

        // 2. Validate Business Rules
        JobPosting jobPosting = applicant.getJobPosting();
        if (jobPosting.getStatus() == JobPostingStatus.EXPIRED) {
            throw new IllegalStateException("Job expired");
        }

        // 3. MAIN LOGIC: Update Status (Dùng method Entity)
        // Nếu đã shortlist rồi thì return true luôn (Idempotency)
        applicant.shortlist();

        // 4. Save DB
        Applicant savedApplicant = applicantRepository.save(applicant);

        // 5. FIRE EVENT (Đã cập nhật)
        // Sử dụng Factory Method 'from' để trích xuất dữ liệu an toàn ngay tại đây
        applicationEventPublisher.publishEvent(ApplicantStatusChangedEvent.from(
                savedApplicant,
                ApplicationStatus.SHORTLISTED
        ));


    }

    @Override
    @Transactional
    public void rejectApplicant(Long applicantId) {
        // 1. Fetch Data
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found"));

        // Lưu ý: Với reject, ta thường KHÔNG cần check Job Expired
        // Vì HR vẫn cần quyền từ chối các hồ sơ kể cả khi Job đã đóng.

        // 2. MAIN LOGIC: Update Status
        // Gọi method trong Entity (Entity đã handle logic: nếu đã Reject rồi thì return true, không lỗi)
        boolean isRejected = applicant.reject();


        // 3. Save DB
        Applicant savedApplicant = applicantRepository.save(applicant);

        // 4. FIRE EVENT
        // Dùng Factory Method 'from' với trạng thái REJECTED
        applicationEventPublisher.publishEvent(ApplicantStatusChangedEvent.from(
                savedApplicant,
                ApplicationStatus.REJECTED
        ));
    }

    @Override
    public boolean existsById(Long applicantId) {
        return applicantRepository.existsById(applicantId);
    }

    @Override
    public User getUserByApplicanId(Long applicantId) {
        return applicantRepository.findUserByApplicantId(applicantId);
    }


    @Override
    public Optional<Applicant> findByUserAndJobPosting(User user, JobPosting jobPosting) {
        return applicantRepository.findExistingApplication(user.getId(), jobPosting.getId());
    }

    @Override
    public List<String> getAvailablePositionsForCompany(Long companyId) {
        // Lấy danh sách JobPosting của công ty
        List<JobPosting> jobPostings = jobPostingRepository.findByCompanyId(companyId);

        // Extract unique titles từ các JobPosting có applicants
        return jobPostings.stream()
                .filter(jp -> jp.getApplicants() != null && !jp.getApplicants().isEmpty())
                .map(JobPosting::getTitle)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }


    @Override
    public Page<Applicant> getFilteredApplicants(Long companyId, String keyword,
                                                 String position, String status, Pageable pageable) {

        // Normalize inputs
        String normalizedKeyword = normalizeString(keyword);
        String normalizedPosition = normalizeString(position);
        ApplicationStatus statusEnum = parseStatus(status);

        // Kiểm tra xem có filter nào không
        boolean hasFilters = normalizedKeyword != null ||
                normalizedPosition != null ||
                statusEnum != null;

        String statusString = statusEnum != null ? statusEnum.name() : null;
        if (!hasFilters) {
            // Không có filter - sử dụng query đơn giản
            return applicantRepository.findByCompanyId(companyId, pageable);
        } else {
            // Có filter - sử dụng query phức tạp
            return applicantRepository.findByCompanyIdWithFilters(
                    companyId, normalizedKeyword, normalizedPosition, statusString, pageable);
        }
    }

    @Override
    @Transactional
    public void deleteAllApplicantsByUserPermanently(Long userId) {
        // Xóa tất cả ApplicationStatusHistory của user
        applicantRepository.hardDeleteApplicationStatusHistoryByUserId(userId);

        // Xóa tất cả ApplicationReview của user
        applicantRepository.hardDeleteApplicationReviewByUserId(userId);

        // Xóa tất cả Notification của user
        applicantRepository.hardDeleteNotificationsByUserId(userId);

        // Xóa tất cả Applicant của user
        applicantRepository.hardDeleteApplicantsByUserId(userId);
    }

    @Transactional
    public void updateStatus(Long applicantId, ApplicationStatus newStatus) {
        // 1. Lấy hồ sơ lên (Kèm theo history)
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ"));

        // 2. Bảo thằng Applicant: "Mày tự đổi trạng thái đi"
        applicant.updateStatusTo(newStatus);

        // 3. Lưu Applicant -> Hibernate tự động lưu luôn các dòng history thay đổi
        applicantRepository.save(applicant);
    }

    @Transactional
    public GetJobApplicantDetailResponse getApplicantDetail(Long id) {

        // 1. COMMAND: Load Entity lên ("Thủ kho" lấy hồ sơ ra bàn)
        // Entity được Hibernate quản lý (Managed) để theo dõi thay đổi.
        Applicant applicant = applicantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // 2. BUSINESS LOGIC: Entity tự xử lý nghiệp vụ "Bóc tem"
        // Nếu đang PENDING -> đổi thành REVIEWING.
        // Hibernate Dirty Checking sẽ tự động lưu thay đổi này cuối hàm (không cần gọi save).
        applicant.markAsReviewed();

        // 3. QUERY: Map sang DTO để trả về ("Thư ký" chép lại thông tin đưa sếp xem)
        // Dùng DTO để bảo mật và tránh lỗi vòng lặp JSON. Object 'app' lúc này đã có status mới nhất.
        return GetJobApplicantDetailResponse.from(applicant, frontendUrl);
    }

    @Override
    @Transactional
    public void scheduleInterviewAndSendEmail(Long applicantId, String subject, String content) {

        // 1. Tìm hồ sơ ứng viên
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new EntityNotFoundException("Applicant not found with id: " + applicantId));

        // 2. Lấy email người nhận
        if (applicant.getUser() == null || applicant.getUser().getContactInfo() == null) {
            throw new IllegalStateException("Applicant user data is incomplete (missing contact info).");
        }
        String candidateEmail = applicant.getUser().getContactInfo().getEmail();

        // =========================================================================================
        // 3. BƯỚC QUAN TRỌNG: TRÍCH XUẤT VÀ TẠO DỮ LIỆU CÓ CẤU TRÚC
        // LƯU Ý: Frontend/Controller lý tưởng nên gửi Date, Time, Type, Location dưới dạng
        // các trường riêng biệt trong DTO, nhưng vì chúng ta chỉ có content, ta phải giả định
        // có một cơ chế để lấy được các giá trị đó hoặc DTO đã được update.
        // =========================================================================================

        // GIẢ ĐỊNH (Placeholder cho dữ liệu đã được Controller/DTO xử lý):
        // Trong môi trường thật, bạn sẽ cần một DTO phức tạp hơn để hứng các giá trị này.
        // Ở đây, ta dùng các giá trị placeholder để code chạy, nhưng cần nhớ phải thay thế
        // bằng logic lấy dữ liệu thật từ request DTO.
        LocalDateTime scheduledDateTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);
        String interviewType = "Online (Google Meet)";
        String locationDetail = "Link họp/Địa chỉ phỏng vấn";

        // 4. GỌI DOMAIN MODEL ĐỂ CHUYỂN TRẠNG THÁI VÀ TẠO SCHEDULE
        // Chuyển trạng thái và tạo Entity InterviewSchedule mới (CascadeType.ALL)
        applicant.scheduleInterview(
                scheduledDateTime,
                interviewType,
                locationDetail,
                content // Nội dung đầy đủ được lưu cho mục đích Audit
        );

        // 5. LƯU THAY ĐỔI VÀO DB
        // Bước này lưu Applicant, tự động INSERT (hoặc UPDATE) vào bảng InterviewSchedule
        // nhờ CascadeType.ALL và quan hệ @OneToOne.
        applicantRepository.save(applicant);

        Company companyToUpdateMetric = applicant.getJobPosting().getCompany();
        companyToUpdateMetric.getCompanyMetric().incrementTotalInterviews(1);

        // 6. Gửi Email
        log.info("Scheduling interview for Applicant ID: {}. Dispatching invitation email...", applicantId);
        emailService.sendInterviewInvitation(candidateEmail, subject, content, applicant.getId());
    }

    @Override
    @Transactional(readOnly = true) // Import org.springframework.transaction.annotation.Transactional
    public GetMyApplicationDetailResponse getApplicantDetailForJobseeker(User currentUser, Long applicantId) {

        // 1. Gọi Repository lấy Entity (Full Graph)
        Applicant applicant = applicantRepository.findDetailForJobSeeker(applicantId, currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ ứng tuyển này hoặc bạn không có quyền truy cập."));

        // 2. Mapping Entity -> DTO (Dùng hàm static factory xịn xò đã viết)
        return GetMyApplicationDetailResponse.fromEntity(applicant);
    }

    @Override
    @Transactional
    public void saveReview(Long applicantId, Double score, String note) {
        // 1. Lấy Applicant
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicationNotFoundException("Applicant not found with ID: " + applicantId));

        // 2. Lấy Company hiện tại (người đang đánh giá)
        Company currentCompany = userService.getCurrentCompany();

        // 3. Ủy quyền logic cho Entity Applicant
        applicant.addOrUpdateReview(score, note, currentCompany);

    }

    @Transactional(readOnly = true)
    @Override
    public ApplicantViewingDto getApplicantViewingData(Company company, ApplicantFilterRequest filter) {
        // 1. Lấy danh sách Job
        if (company == null) {
            log.warn("[getApplicantViewingData] Company is null. Returning empty DTO.");
            return new ApplicantViewingDto(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        }
        List<JobPosting> allJobs = jobPostingRepository.findByCompanyId(company.getId());

        if (allJobs.isEmpty()) {
            // Trả về DTO rỗng
            return new ApplicantViewingDto(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyMap());
        }

        // 2. Xác định Job hiện tại
        Long currentJobId = (filter.getJobId() != null) ? filter.getJobId() : allJobs.get(0).getId();
        JobPosting currentJobEntity = allJobs.stream()
                .filter(j -> j.getId().equals(currentJobId))
                .findFirst()
                .orElse(allJobs.get(0));

        // 3. Lấy danh sách ứng viên
        List<Applicant> applicantEntities = applicantRepository.findByJobPostingIdAndStatus(
                currentJobEntity.getId(),
                filter.getStatus()
        );

        // [NEW] Lấy số lượng ứng viên theo từng trạng thái
        List<ApplicantViewingDto.StatusCount> statusCountRecords = applicantRepository.countApplicantsByStatusForJob(currentJobEntity.getId());
        Map<String, Long> statusCounts = statusCountRecords.stream()
                .collect(Collectors.toMap(
                        record -> record.status().name(),
                        ApplicantViewingDto.StatusCount::count
                ));

        // [NEW] Lấy số lượng ứng viên cho TẤT CẢ các job trong 1 query
        Map<Long, Long> applicantCountsByJobId = applicantRepository.countApplicantsByJobForCompany(company.getId())
                .stream()
                .collect(Collectors.toMap(
                        JobApplicantCount::getJobId,
                        JobApplicantCount::getApplicantCount
                ));


        // 4. Mapping (Logic phức tạp nằm gọn ở đây)
        List<ApplicantViewingDto.JobInfo> jobInfos = allJobs.stream()
                // Truyền map count vào hàm mapToJobInfo
                .map(job -> mapToJobInfo(job, currentJobEntity.getId(), applicantCountsByJobId))
                .toList();

        ApplicantViewingDto.JobInfo currentJobInfo = mapToJobInfo(currentJobEntity, currentJobEntity.getId(), applicantCountsByJobId);

        List<ApplicantViewingDto.CandidateInfo> candidateInfos = applicantEntities.stream()
                .map(this::mapToCandidateInfo) // Gọi hàm private bên dưới
                .toList();

        return new ApplicantViewingDto(currentJobInfo, jobInfos, candidateInfos, statusCounts);
    }

    // =========================================================================
    //  [NEW] BULK ACTION IMPLEMENTATIONS
    // =========================================================================

    @Override
    @Transactional
    public void bulkShortlist(List<Long> applicantIds) {
        // 1. Lấy tất cả Applicant trong 1 câu lệnh SQL duy nhất
        List<Applicant> applicants = applicantRepository.findAllById(applicantIds);

        // Kiểm tra xem có tìm thấy đủ số lượng không (tùy chọn)
        if (applicants.size() != applicantIds.size()) {

            // Ném ra một lỗi cụ thể để báo cho người dùng
            throw new BulkApplicantActionException(
                    String.format("Could not process all applicants.  applicant(s) not found or already processed. Please refresh and try again.")
            );
        }

        List<Applicant> changedApplicants = new ArrayList<>();

        // 2. Lặp qua các entity trong bộ nhớ để xử lý logic
        for (Applicant applicant : applicants) {
            try {
                applicant.shortlist(); // Gọi logic từ entity
                changedApplicants.add(applicant);
            } catch (ApplicantStatusTransitionException e) {
                // Xử lý lỗi cho một ứng viên cụ thể
                // Ném lại exception để ROLLBACK toàn bộ transaction
                throw new BulkApplicantActionException(
                        String.format("Failed to shortlist applicant '%s': %s",
                                applicant.getUser().getPersonalInfo().getFullName(), e.getMessage())
                );
            }
        }

        // 3. Bắn sự kiện cho các ứng viên đã thay đổi
        // (Có thể tối ưu hơn bằng cách tạo một sự kiện bulk duy nhất)
        changedApplicants.forEach(app ->
                applicationEventPublisher.publishEvent(
                        ApplicantStatusChangedEvent.from(app, ApplicationStatus.SHORTLISTED)
                )
        );

        log.info("Successfully processed bulk shortlist for {} applicants.", changedApplicants.size());
    }

    @Override
    @Transactional
    public void bulkReject(List<Long> applicantIds) {
        List<Applicant> applicants = applicantRepository.findAllById(applicantIds);

        for (Applicant applicant : applicants) {
            applicant.reject(); // Entity đã có logic idempotency (từ chối lại vẫn OK)
        }

        applicants.forEach(app ->
                applicationEventPublisher.publishEvent(
                        ApplicantStatusChangedEvent.from(app, ApplicationStatus.REJECTED)
                )
        );
        log.info("Successfully processed bulk reject for {} applicants.", applicants.size());
    }

    @Override
    @Transactional
    public void bulkScheduleInterview(List<Long> applicantIds, String subject, String content) {
        List<Applicant> applicants = applicantRepository.findAllById(applicantIds);

        if (applicants.size() != applicantIds.size()) {
            throw new BulkApplicantActionException("Could not process all applicants. Some were not found. Please refresh and try again.");
        }

        // ✨ [NEW] Tăng chỉ số phỏng vấn một lần cho cả nhóm
        if (!applicants.isEmpty()) {
            // Lấy công ty từ ứng viên đầu tiên (vì tất cả đều thuộc cùng 1 job)
            Company companyToUpdateMetric = applicants.get(0).getJobPosting().getCompany();
            companyToUpdateMetric.getCompanyMetric().incrementTotalInterviews(applicants.size());
        }

        // GIẢ ĐỊNH (Placeholder cho dữ liệu đã được Controller/DTO xử lý):
        // Các thông tin này sẽ được lấy từ request trong tương lai, hiện tại dùng giá trị tạm
        LocalDateTime scheduledDateTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0);
        String interviewType = "Online (Google Meet)";
        String locationDetail = "Link họp/Địa chỉ phỏng vấn sẽ được cung cấp trong email.";

        for (Applicant applicant : applicants) {
            try {
                // GỌI DOMAIN MODEL ĐỂ CHUYỂN TRẠNG THÁI VÀ TẠO SCHEDULE
                applicant.scheduleInterview(
                        scheduledDateTime,
                        interviewType,
                        locationDetail,
                        content
                );

                // Gửi Email cho từng ứng viên
                String candidateEmail = applicant.getUser().getContactInfo().getEmail();
                emailService.sendInterviewInvitation(candidateEmail, subject, content, applicant.getId());

            } catch (ApplicantStatusTransitionException e) {
                // Nếu một ứng viên thất bại, ném lỗi để rollback toàn bộ
                throw new BulkApplicantActionException(String.format("Failed to schedule interview for '%s': %s", applicant.getUser().getPersonalInfo().getFullName(), e.getMessage()));
            }
        }

        log.info("Successfully processed and sent bulk interview invitations for {} applicants.", applicants.size());
    }


    private ApplicantViewingDto.JobInfo mapToJobInfo(JobPosting job, Long activeId, Map<Long, Long> counts) {
        // Lấy count từ map, nếu không có thì mặc định là 0
        long applicantCount = counts.getOrDefault(job.getId(), 0L);
        return new ApplicantViewingDto.JobInfo(
                job.getId(),
                job.getTitle(),
                job.getLocation() != null ? job.getLocation().toString() : "Unknown",
                job.getPostedDate() != null ? job.getPostedDate().atStartOfDay() : LocalDateTime.now(),
                job.getId().equals(activeId),
                applicantCount
        );
    }

    private ApplicantViewingDto.CandidateInfo mapToCandidateInfo(Applicant app) {
        // Xử lý null check an toàn ở đây
        String fullName = (app.getUser() != null && app.getUser().getPersonalInfo() != null)
                ? app.getUser().getPersonalInfo().getFullName() : "Unknown";

        String email = (app.getUser() != null && app.getUser().getContactInfo() != null)
                ? app.getUser().getContactInfo().getEmail() : "";

        String phone = (app.getUser() != null && app.getUser().getContactInfo() != null)
                ? app.getUser().getContactInfo().getPhoneNumber() : "";

        String status = (app.getCurrentStatusHistory() != null && app.getCurrentStatusHistory().getStatus() != null)
                ? app.getCurrentStatusHistory().getStatus().name()
                : "UNKNOWN";

        String avatarUrl = app.getUser().getProfilePicture();

        return new ApplicantViewingDto.CandidateInfo(
                app.getId(),
                fullName,
                email,
                getInitials(fullName),
                avatarUrl,
                phone,
                app.getApplicationDate(),
                status,
                app.getResume()
        );
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "??";
        return name.trim().substring(0, Math.min(2, name.length())).toUpperCase();
    }

    // Helper methods
    private String normalizeString(String input) {
        return (input != null && !input.trim().isEmpty()) ? input.trim() : null;
    }

    private ApplicationStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null; // Invalid status, ignore
        }
    }

    @Override
    @Transactional
    public void withdrawApplication(Long applicantId, Long userId) {
        // 1. Lấy entity từ DB
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicationNotFoundException("Can not find any applicnat with: " + applicantId));

        // 2. Ủy quyền toàn bộ logic nghiệp vụ cho chính Entity
        applicant.withdraw(userId);


        Applicant savedApplicant = applicantRepository.save(applicant);

        // 4. Bắn ra sự kiện cụ thể cho hành động rút đơn
        applicationEventPublisher.publishEvent(
                ApplicantWithdrewEvent.from(savedApplicant)
        );
    }

    @Override
    public List<Applicant> getApplicantsByJobPostingId(Long jobPostingId) {
        return applicantRepository.findByJobPostingId(jobPostingId);
    }

}
