package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.event.company.SubscriptionDowngradedEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingCreatedEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingDeletedEvent;
import com.example.baoNgoCv.jpa.projection.applicant.ApplicantInfoProjection;
import com.example.baoNgoCv.jpa.projection.company.CompanyJobManagementProjection;
import com.example.baoNgoCv.jpa.projection.company.SubscriptionUsageProjection;
import com.example.baoNgoCv.model.dto.JobPostingMetricsDTO;
import com.example.baoNgoCv.model.dto.company.GetJobpostingManagingRequest;
import com.example.baoNgoCv.model.dto.company.GetJobpostingManagingResponse;
import com.example.baoNgoCv.model.dto.company.PutJobPostingRequest;
import com.example.baoNgoCv.model.dto.company.PutJobPostingResponse;
import com.example.baoNgoCv.model.dto.homepage.GetHomePageResponse;
import com.example.baoNgoCv.model.dto.jobposting.*;
import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import com.example.baoNgoCv.exception.companyException.CompanyNotFoundException;
import com.example.baoNgoCv.exception.jobpostingException.*;
import com.example.baoNgoCv.mapper.JobPostingMapper;
import com.example.baoNgoCv.mapper.JobSavedMapper;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.model.enums.*;
import com.example.baoNgoCv.jpa.repository.*;
import com.example.baoNgoCv.jpa.projection.company.CompanyInfoProjection;

import com.example.baoNgoCv.jpa.projection.jobPosting.*;
import com.example.baoNgoCv.model.valueObject.SubscriptionDetails;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobPostingServiceImpl implements JobPostingService {

    @Value("${app.jobposting.expiration-threshold-days:7}")
    private int EXPIRATION_THRESHOLD_DAYS;


    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final ApplicantRepository applicantRepository;
    private final JobSavedRepository jobSavedRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingMapper jobPostingMapper;
    private final JobSavedMapper jobSavedMapper;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getJobPostingByIndustryId(Long industryId) {
        //1. Phương thức này chưa được triển khai, trả về null.
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<JobPosting> getByCompanyId(Long id) {
        //1. Gọi repository để tìm tất cả các bài đăng tuyển dụng theo ID của công ty.
        return jobPostingRepository.findByCompanyId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobCardProjection> searchJobPostings(GetJobSearchRequest searchRequest) {
        //1. Chuẩn hóa và xác thực các tham số tìm kiếm đầu vào.
        GetJobSearchRequest normalizedRequest = searchRequest.validateAndNormalize();

        //2. Tạo đối tượng Sort để sắp xếp kết quả.
        Sort sort = Sort.by(Sort.Direction.fromString(normalizedRequest.sortOrder()), normalizedRequest.sortBy());

        //3. Tạo đối tượng Pageable để phân trang.
        Pageable pageable = PageRequest.of(normalizedRequest.page(), normalizedRequest.size(), sort);

        //4. Chuyển đổi các danh sách enum thành danh sách chuỗi để tương thích với câu lệnh query.
        List<String> locationStrings = normalizedRequest.locations() == null || normalizedRequest.locations().isEmpty() ? List.of() : normalizedRequest.locations().stream().map(Enum::name).toList();
        List<String> experienceStrings = normalizedRequest.experiences() == null || normalizedRequest.experiences().isEmpty() ? List.of() : normalizedRequest.experiences().stream().map(Enum::name).toList();
        List<String> salaryRangeStrings = normalizedRequest.salaryRanges() == null || normalizedRequest.salaryRanges().isEmpty() ? List.of() : normalizedRequest.salaryRanges().stream().map(Enum::name).toList();
        List<String> jobTypeStrings = normalizedRequest.jobTypes() == null || normalizedRequest.jobTypes().isEmpty() ? List.of() : normalizedRequest.jobTypes().stream().map(Enum::name).toList();
        List<String> industryStrings = normalizedRequest.industries() == null || normalizedRequest.industries().isEmpty() ? List.of() : normalizedRequest.industries().stream().map(Enum::name).toList();
        String jobStatusString = normalizedRequest.jobStatus() != null ? normalizedRequest.jobStatus().name() : null;

        //5. Gọi repository để thực hiện tìm kiếm với các tham số đã xử lý.
        return jobPostingRepository.searchJobPostings(normalizedRequest.keyword(), locationStrings, locationStrings.size(), experienceStrings, experienceStrings.size(), salaryRangeStrings, salaryRangeStrings.size(), jobTypeStrings, jobTypeStrings.size(), industryStrings, industryStrings.size(), jobStatusString, normalizedRequest.postedAfter(), normalizedRequest.deadlineBefore(), pageable);
    }


    @Override
    @Transactional(readOnly = true)
    public GetHomePageResponse getHomePageData() {
        // 1. Lấy top 9 bài theo Trending Score giảm dần
        Page<JobPosting> jobPostingPage = jobPostingRepository.findAllByOrderByTrendingScoreDesc(Pageable.ofSize(9));

        // 2. Convert Entity -> DTO
        List<JobCardDTO> jobCards = jobPostingPage.stream().map(job -> {

            // Xử lý requirements (Lấy tối đa 2 cái đầu tiên)
            List<String> topReqs = (job.getRequirements() != null)
                    ? job.getRequirements().stream().limit(2).toList()
                    : List.of();

            return JobCardDTO.builder()
                    .id(job.getId())
                    .title(job.getTitle())

                    // Null-safe check cho Company
                    .companyName(job.getCompany() != null ? job.getCompany().getName() : "Unknown")
                    .companyLogo(job.getCompany() != null ? job.getCompany().getCompanyLogo() : null)

                    .location(job.getLocation())
                    .jobType(job.getJobType())
                    .salaryRange(job.getSalaryRange())
                    .experience(job.getExperience())
                    .industry(job.getIndustry())
                    .postedDate(job.getPostedDate())
                    .applicationDeadline(job.getApplicationDeadline())

                    // [FIX] Map đúng các trường vừa thêm vào Entity
                    .applicantCount(job.getJobMetric().getReceivedCount()) // Map với receivedCount
                    .maxApplicants(job.getTargetHires())    // Map với targetHires
                    .viewCount(job.getJobMetric().getViewCount())          // Map với viewCount

                    // Logic Trending: Score > 50 được coi là trending
                    .isTrending(job.getJobMetric().getTrendingScore() != null &&
                            job.getJobMetric().getTrendingScore().compareTo(BigDecimal.valueOf(50)) > 0)
                    .trendingScore(job.getJobMetric().getTrendingScore() != null
                            ? job.getJobMetric().getTrendingScore().doubleValue()
                            : 0.0)

                    .topRequirements(topReqs)
                    .build();
        }).toList(); // Dùng .toList() cho gọn (Java 16+)

        Integer totalJobs = jobPostingRepository.countActiveJobs();

        return GetHomePageResponse.builder()
                .jobCards(jobCards)
                .totalJobs(totalJobs)
                .featuredJobs(9)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<JobPosting> getJobPostingById(long id) {
        //1. Gọi repository để tìm một bài đăng tuyển dụng theo ID.
        return jobPostingRepository.findById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<JobPosting> getCompanyJobPostings(Long companyId, Pageable pageable) {
        //1. Gọi repository để tìm các bài đăng của một công ty với phân trang.
        return jobPostingRepository.findByCompanyId(companyId, pageable);
    }


    @Override
    @Transactional(readOnly = true)
    public JobPostingMetricsDTO calculateJobPostingMetrics(Long companyId) {
        //1. Lấy tất cả các bài đăng của công ty.
        List<JobPosting> allJobs = jobPostingRepository.findByCompanyId(companyId);

        //2. Tính toán các chỉ số khác nhau dựa trên trạng thái của các bài đăng.
        long totalJobs = allJobs.size();
        long activeJobs = allJobs.stream().filter(job -> job.getStatus() == JobPostingStatus.OPEN).count();
        long closedJobs = allJobs.stream().filter(job -> job.getStatus() == JobPostingStatus.CLOSED).count();
        long expiredJobs = allJobs.stream().filter(job -> job.getStatus() == JobPostingStatus.EXPIRED).count();
        long filledJobs = allJobs.stream().filter(job -> job.getStatus() == JobPostingStatus.FILLED).count();
        long totalApplicants = 2;
        long expiringSoon = allJobs.stream().filter(job -> job.getApplicationDeadline() != null && job.getStatus() == JobPostingStatus.OPEN && job.getApplicationDeadline().isBefore(LocalDate.now().plusDays(7)) && job.getApplicationDeadline().isAfter(LocalDate.now())).count();

        //3. Trả về DTO chứa các chỉ số đã tính toán.
        return new JobPostingMetricsDTO(totalJobs, activeJobs, totalApplicants, expiringSoon, closedJobs, expiredJobs, filledJobs);
    }

    @Override
    @Transactional
    public void deleteJobPosting(Long jobPostingId) {
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new JobNotFoundExceptionJson("Job posting not found with id: " + jobPostingId));

        List<ApplicantInfoProjection> applicantProjections = applicantRepository.findApplicantInfoByJobId(jobPostingId);
        List<JobPostingDeletedEvent.ApplicantInfo> applicantInfos = applicantProjections.stream()
                .map(p -> new JobPostingDeletedEvent.ApplicantInfo(p.getUserId(), p.getUserEmail(), p.getUserFullName(), p.getUsername()))
                .toList();

        JobPostingDeletedEvent event = new JobPostingDeletedEvent(
                jobPosting.getTitle(),
                jobPosting.getCompany().getName(),
                applicantInfos
        );
        applicationEventPublisher.publishEvent(event);

        jobPostingRepository.deleteById(jobPostingId);
    }

    @Override
    @Transactional
    public PostJobResponse publishJobPosting(PostJobRequest createRequest, Long id) {
        //1. Lấy thông tin công ty đang đăng nhập.
        Company currentCompany = companyRepository.findById(id).orElseThrow(() -> new CompanyNotFoundException());

        //2. Lấy thông tin gói cước của công ty.
        SubscriptionDetails subDetails = currentCompany.getSubscriptionDetails();
        if (subDetails == null) {
            throw new IllegalStateException("Company does not have subscription details.");
        }

        //3. Luôn làm mới trạng thái gói cước, nếu hết hạn sẽ tự động hạ cấp.
        AccountTier oldTier = subDetails.getAccountTier();
        boolean wasDowngraded = subDetails.validateAndRefreshState();

        //4. Nếu có sự hạ cấp, bắn ra sự kiện để thông báo.
        if (wasDowngraded) {
            applicationEventPublisher.publishEvent(new SubscriptionDowngradedEvent(this, currentCompany.getId(), currentCompany.getName(), currentCompany.getContactEmail(), oldTier));
        }

        //5. Hỏi đối tượng SubscriptionDetails xem có thể đăng bài mới không.
        if (!subDetails.canPostNewJob()) {
            // ❌ LOGGING ERROR TRƯỚC KHI THROW EXCEPTION
            log.error("Job posting blocked for companyId: {}. Quota exceeded.", currentCompany.getId());

            // Log chi tiết hơn để debug
            log.warn("Details - Current jobs in cycle: {}, Max jobs for {} plan: {}",
                    subDetails.getJobPostingsThisCycle(),
                    subDetails.getAccountTier().name(),
                    subDetails.getAccountTier().getMaxJobPostings());

            // Ném exception để dừng quy trình
            throw new JobPostingLimitExceededException(
                    String.format("You have reached the maximum of %d job postings for your %s plan.",
                            subDetails.getAccountTier().getMaxJobPostings(),
                            subDetails.getAccountTier().name())
            );
        }


        //7. Nếu hợp lệ, tạo và lưu bài đăng mới.
        JobPosting jobposting = JobPosting.createNewJobPosting(createRequest, currentCompany);
        JobPosting savedJobPosting = jobPostingRepository.save(jobposting);

        //8. Tăng bộ đếm số lượng bài đã đăng trong chu kỳ.
        subDetails.incrementJobPostingsThisCycle();
        companyRepository.save(currentCompany);

     // ✨ [NEW] Tăng chỉ số openJobs trong CompanyMetric
        if (currentCompany.getCompanyMetric() == null) {
            // 1. Tạo mới bằng hàm vừa thêm
            CompanyMetric newMetric = CompanyMetric.createDefault(currentCompany);

            // 2. Gán vào company
            currentCompany.setCompanyMetric(newMetric);

            // 3. (Quan trọng) Nếu không có CascadeType.ALL, bạn phải save metric này trước
            // companyMetricRepository.save(newMetric);
        }

// Giờ thì gọi incOpenJob() thoải mái
        currentCompany.getCompanyMetric().incOpenJob();

        //9. Bắn sự kiện để thông báo cho những người theo dõi công ty.
        List<String> followerUsernames = currentCompany.getFollowers().stream().map(User::getUsername).toList();
        applicationEventPublisher.publishEvent(new JobPostingCreatedEvent(savedJobPosting.getId(), followerUsernames, currentCompany.getContactEmail(), savedJobPosting.getTitle(), currentCompany.getName(), currentCompany.getCompanyLogo()));

        //10. Trả về response chứa thông tin bài đăng đã tạo.
        return PostJobResponse.from(savedJobPosting);
    }

    @Override
    @Transactional(readOnly = true)
    public GetJobResponse getPostAJobPage(Authentication auth) {
        //1. Lấy thông tin công ty từ đối tượng Authentication.
        Company company = (Company) auth.getPrincipal();
        //2. Trả về DTO chứa tên công ty.
        return new GetJobResponse(company.getName());
    }

    @Override
    @Transactional
    public void updateJobPostingStatus(Long jobPostingId, JobPostingStatus jobPostingStatus) {
        //1. Tìm bài đăng theo ID.
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId).orElseThrow(() -> new RuntimeException("Job posting not found with id: " + jobPostingId));

        //2. Kiểm tra xem bài đăng đã hết hạn chưa.
        if (jobPosting.getStatus() == JobPostingStatus.EXPIRED) {
            throw new IllegalStateException("Cannot update job posting status as it is already " + jobPosting.getStatus());
        }

        //3. Nếu trạng thái mới là EXPIRED, gửi thông báo đến những người đã lưu tin.
        if (jobPostingStatus == JobPostingStatus.EXPIRED) {
            List<JobSaved> jobSavedList = jobSavedRepository.findByJobPosting(jobPosting);
            for (JobSaved jobSaved : jobSavedList) {
                User user = jobSaved.getUser();
                Notification notification = new Notification();
                notification.setTitle("Job posting in your saved list expired");
                notification.setSenderUser(userService.findByUsername("admin01"));
                notification.setCreatedAt(LocalDateTime.now());
                notification.setRecipientUser(user);
                notification.setType(NotificationType.JOB_CLOSED);
                notification.setAvatar(userService.findByUsername("admin01").getProfilePicture());
                notification.setRead(false);
                Notification newNoti = notificationRepository.save(notification);
                newNoti.setHref("/jobseeker/job-save?notiId=" + newNoti.getId());
                notificationRepository.save(newNoti);
                notificationService.sendReviewNotificationToUser("Job posting in your saved list expired", user, userService.findByUsername("admin01").getProfilePicture(), "Admin", "/jobseeker/job-save?notiId=" + newNoti.getId());
            }
        }

        Company company = jobPosting.getCompany();
        company.getCompanyMetric().incOpenJob();

    }

    @Override
    @Transactional
    public void deleteJobPostingById(Long jobPostingId) {
        //1. Kiểm tra sự tồn tại của bài đăng.
        if (!jobPostingRepository.existsById(jobPostingId)) {
            throw new RuntimeException("JobPosting not found with ID: " + jobPostingId);
        }
        //2. Xóa cứng các bản ghi ứng viên liên quan.
        applicantRepository.hardDeleteByJobPostingId(jobPostingId);
        //3. Xóa mềm bài đăng (nếu có cấu hình soft delete).
        jobPostingRepository.deleteById(jobPostingId);
    }


    private void notifyFollowers(Company company, JobPosting jobPosting) {
        //1. Lấy danh sách người theo dõi của công ty.
        Set<User> followers = company.getFollowers();
        //2. Lặp qua từng người theo dõi và gửi thông báo.
        followers.forEach(user -> {
            //3. Tạo đối tượng Notification.
            Notification notification = new Notification();
            notification.setTitle("A new job posting is available!");
            notification.setSenderCompany(company);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRecipientUser(user);
            notification.setType(NotificationType.NEW_JOB_POSTING);
            notification.setAvatar(company.getCompanyLogo());
            notification.setRead(false);

            //4. Lưu thông báo vào DB để lấy ID.
            Notification savedNotification = notificationRepository.save(notification);
            //5. Tạo link và cập nhật lại thông báo.
            savedNotification.setHref("/jobseeker/job-detail/" + jobPosting.getId() + "?notiId=" + savedNotification.getId());
            notificationRepository.save(savedNotification);

            //6. Gửi thông báo real-time qua WebSocket.
            notificationService.sendReviewNotificationToUser("A new job posting is available!", user, company.getCompanyLogo(), company.getName(), savedNotification.getHref());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public GetJobpostingManagingResponse getJobPostingManagement(Pageable pageable) {

        // BƯỚC 1: Lấy thông tin công ty đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof Company)) {
            throw new SecurityException("User is not authenticated or not a company");
        }
        Company company = (Company) authentication.getPrincipal();
        Long companyId = company.getId();

        // BƯỚC 2: Gọi các truy vấn chuyên biệt để lấy dữ liệu
        // (Để tối ưu hơn nữa, bạn có thể chạy 3 query này bất đồng bộ với CompletableFuture)

        // Query 1: Lấy danh sách job đã phân trang (sử dụng projection)
        Page<CompanyJobManagementProjection> jobPage = companyRepository.findJobPostingsForManagement(companyId, pageable);

        // Chuyển đổi từ Page<Projection> sang List<DTO>
        List<GetJobpostingManagingResponse.CompanyJobManagement> jobPostingsDtoList =
                jobPage.getContent().stream()
                        .map(projection -> new GetJobpostingManagingResponse.CompanyJobManagement(
                                projection.getId(),
                                projection.getTitle(),
                                projection.getJobType(),
                                projection.getLocation(),
                                projection.getSalaryRange(),
                                projection.getExperience(),
                                projection.getIndustry(),
                                projection.getApplicationDeadline(),
                                projection.getStatus(),
                                projection.getApplicantCount(),
                                projection.getMaxApplicants(),
                                projection.getCompanyName(),
                                projection.getDescriptions(),
                                projection.getRequirements(),
                                projection.getBenefits(),
                                projection.getEditCount()
                        )).toList();

        // Query 2: Lấy thông tin sử dụng gói cước (tính toán tại DB)
        GetJobpostingManagingResponse.SubscriptionUsageInfo usageInfo = companyRepository
                .findActiveSubscriptionUsageByCompanyId(companyId)
                .map(projection -> new GetJobpostingManagingResponse.SubscriptionUsageInfo(
                        projection.used(),
                        projection.limit(),
                        projection.accountTier().getDisplayName(),
                        projection.percentage()
                ))
                .orElseThrow(() -> new IllegalStateException("Can not find the company's subscription: " + companyId));

        // Query 3: Lấy thông tin thống kê tổng quan (tính toán tại DB)
        LocalDate expirationThreshold = LocalDate.now().plusDays(EXPIRATION_THRESHOLD_DAYS);
        GetJobpostingManagingResponse.JobStatistics statistics = companyRepository
                .getJobStatisticsByCompanyId(companyId, expirationThreshold)
                .map(projection -> new GetJobpostingManagingResponse.JobStatistics(
                        projection.totalJobs(),
                        projection.openJobs(),
                        projection.closedJobs(),
                        projection.totalApplications(),
                        projection.expiringSoonCount()
                ))
                .orElse(new GetJobpostingManagingResponse.JobStatistics(0, 0, 0, 0, 0));

        // BƯỚC 3: Tạo đối tượng thông tin phân trang
        GetJobpostingManagingResponse.PaginationData pagination = new GetJobpostingManagingResponse.PaginationData(
                jobPage.getNumber(),
                jobPage.getTotalPages(),
                jobPage.getTotalElements()
        );

        // BƯỚC 4: Lắp ráp DTO cuối cùng và trả về
        return new GetJobpostingManagingResponse(jobPostingsDtoList, pagination, usageInfo, statistics);
    }

    @Override
    @Transactional(readOnly = true)
    public GetJobDetailResponse getJobDetail(Long jobId) {
        //1. Lấy thông tin người dùng hiện tại (nếu có).
        Optional<Authentication> authOpt = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
        Long userId = authOpt.filter(auth -> auth.getPrincipal() instanceof User).map(auth -> ((User) auth.getPrincipal()).getId()).orElse(null);

        //2. Lấy thông tin chi tiết của bài đăng cùng với trạng thái của người dùng (đã lưu, đã ứng tuyển).
        JobDetailWithUserStatusProjection projection = jobPostingRepository.findJobDetailWithUserStatus(jobId, userId).orElseThrow(() -> new JobNotFoundExceptionHtml("Job not found: " + jobId));

        //3. Lấy các danh sách mô tả, yêu cầu, và quyền lợi.
        List<String> descriptions = jobPostingRepository.findDescriptionsByJobId(jobId);
        List<String> requirements = jobPostingRepository.findRequirementsByJobId(jobId);
        List<String> benefits = jobPostingRepository.findBenefitsByJobId(jobId);

        //4. Tìm các công việc liên quan.
        List<RelevantJobProjection> relevantJobs = jobPostingRepository.findRelevantJobsProjection(projection.getIndustry(), jobId, PageRequest.of(0, 8));

        //5. Chuyển đổi tất cả dữ liệu đã lấy được sang DTO response.
        GetJobDetailResponse response = jobPostingMapper.toResponseFromProjection(projection, descriptions, requirements, benefits, relevantJobs);
        log.debug("Full response: {}", response);

        jobPostingRepository.incrementViewCount(jobId);

        return response;


    }

    @Override
    @Transactional(readOnly = true)
    public GetApplyJobResponse getApplyJobData(Long jobPostingId) {
        //1. Lấy thông tin người dùng hiện tại.
        User currentUser = userService.getCurrentUser();
        Long userId = (currentUser != null) ? currentUser.getId() : null;

        //2. Lấy thông tin bài đăng.
        JobPostingForApplyJobProjection jobPosting = jobPostingRepository.findJobPostingForApplyJobProjection(jobPostingId).orElseThrow(() -> new JobNotFoundExceptionHtml("Job posting not found with ID: " + jobPostingId));

        //3. Lấy thông tin công ty.
        CompanyInfoProjection company = companyRepository.findCompanyInfoByJobPostingId(jobPostingId).orElse(null);

        //4. Lấy thông tin người dùng (nếu có).
        Optional<UserForApplyJobProjection> userOpt = (userId != null) ? userRepository.findUserProjection(userId) : Optional.empty();
        UserForApplyJobProjection user = userOpt.orElse(null);

        //5. Kiểm tra xem người dùng đã ứng tuyển vào công việc này chưa.
        Optional<Long> applicationIdOpt = (userId != null) ? applicantRepository.findApplicationIdByUserIdAndJobPostingId(userId, jobPostingId) : Optional.empty();
        boolean hasApplied = applicationIdOpt.isPresent();
        Long applicationId = applicationIdOpt.orElse(null);

        //6. Kiểm tra xem công việc đã hết hạn chưa.
        boolean isExpired = jobPosting.getApplicationDeadline() != null && jobPosting.getApplicationDeadline().isBefore(LocalDate.now());

        //7. Tạo và trả về DTO response.
        GetApplyJobResponse response = new GetApplyJobResponse(jobPosting, company, user, hasApplied, isExpired, applicationId);
        log.debug("Complete response: {}", response);
        return response;
    }

    @Override
    @Transactional
    public void saveJob(Long jobPostingId) {
        //1. Tìm bài đăng theo ID.
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId).orElseThrow(() -> new JobNotFoundExceptionJson("Job posting not found."));
        //2. Lấy người dùng hiện tại.
        User currentUser = userService.getCurrentUser();
        //3. Kiểm tra xem công việc đã được lưu chưa.
        if (jobSavedRepository.existsByJobPostingAndUser(jobPosting, currentUser)) {
            throw new JobAlreadySavedException("You have already saved this job.");
        }
        //4. Tạo và lưu đối tượng JobSaved.
        JobSaved jobSaved = JobSaved.create(jobPosting, currentUser);

        jobPosting.getJobMetric().incSave();

        jobSavedRepository.save(jobSaved);
    }

    @Override
    @Transactional
    public void unSaveJob(Long jobPostingId) {
        // 1. Tìm bài đăng
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new JobNotFoundExceptionJson("Job posting not found."));

        // 2. Lấy user hiện tại
        User currentUser = userService.getCurrentUser();

        // 3. Tìm JobSaved tương ứng
        JobSaved jobSaved = jobSavedRepository
                .findByJobPostingAndUser(jobPosting, currentUser)
                .orElseThrow(() -> new JobNotFoundExceptionJson("Job not saved by this user."));

        // 4. Xóa bản ghi saved
        jobSavedRepository.delete(jobSaved);

        // 5. Giảm saveCount trong JobMetric
        jobPosting.getJobMetric().decSave();
    }


    @Override
    @Transactional
    public GetJobSaveResponse getSavedJobsPage(GetJobSaveRequest request) {
        //1. Lấy người dùng hiện tại.
        User user = userService.getCurrentUser();
        //2. Nếu có ID thông báo, đánh dấu là đã đọc.
        if (request.getNotiId() != null) {
            notificationRepository.findById(request.getNotiId()).ifPresent(noti -> {
                noti.setRead(true);
                notificationRepository.save(noti);
            });
        }
        //3. Lấy danh sách các công việc đã lưu với phân trang.
        Pageable pageable = request.toPageable();
        Page<SavedJobProjection> savedPage = jobSavedRepository.findSavedJobsProjectionByUserId(user.getId(), pageable);
        //4. Chuyển đổi từ projection sang DTO.
        List<GetJobSaveResponse.SavedJobsData.SavedJobItem> jobs = savedPage.getContent().stream().map(jobSavedMapper::toSavedJobItem).collect(Collectors.toList());
        //5. Tải hàng loạt các thông tin phụ (mô tả, yêu cầu, quyền lợi) để tránh N+1 query.
        enrichAllJobsWithCollections(jobs);
        //6. Xây dựng và trả về DTO response hoàn chỉnh.
        return GetJobSaveResponse.builder().savedJobsData(GetJobSaveResponse.SavedJobsData.builder().savedJobs(jobs).totalCount((int) savedPage.getTotalElements()).hasJobs(!jobs.isEmpty()).build()).paginationData(GetJobSaveResponse.PaginationData.fromPage(savedPage)).userData(GetJobSaveResponse.UserData.builder().userId(user.getId()).username(user.getUsername()).build()).build();
    }

    @Override
    @Transactional
    public PutJobPostingResponse updateJobPosting(Long id, PutJobPostingRequest request) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundExceptionJson("Job posting not found with id: " + id));

        jobPosting.updateFromRequest(request);

        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);

        return PutJobPostingResponse.fromEntity(savedJobPosting);
    }

    private void enrichAllJobsWithCollections(List<GetJobSaveResponse.SavedJobsData.SavedJobItem> jobs) {
        //1. Nếu không có công việc nào, không làm gì cả.
        if (jobs.isEmpty()) return;
        //2. Lấy danh sách ID của các công việc.
        List<Long> jobIds = jobs.stream().map(GetJobSaveResponse.SavedJobsData.SavedJobItem::getJobId).collect(Collectors.toList());
        //3. Tải hàng loạt các thông tin phụ trong 3 câu lệnh query.
        Map<Long, List<String>> descriptionsMap = loadDescriptionsMap(jobIds);
        Map<Long, List<String>> requirementsMap = loadRequirementsMap(jobIds);
        Map<Long, List<String>> benefitsMap = loadBenefitsMap(jobIds);
        //4. Gán các thông tin phụ vào từng DTO công việc.
        jobs.forEach(job -> {
            job.setDescriptions(descriptionsMap.getOrDefault(job.getJobId(), List.of()));
            job.setRequirements(requirementsMap.getOrDefault(job.getJobId(), List.of()));
            job.setBenefits(benefitsMap.getOrDefault(job.getJobId(), List.of()));
            formatDisplayFields(job);
        });
    }

    private Map<Long, List<String>> loadDescriptionsMap(List<Long> jobIds) {
        //1. Tải tất cả các mô tả cho danh sách ID công việc.
        List<JobSavedRepository.JobDescriptionProjection> projections = jobSavedRepository.findDescriptionsByJobIds(jobIds);
        //2. Nhóm các mô tả theo ID công việc.
        return projections.stream().collect(Collectors.groupingBy(JobSavedRepository.JobDescriptionProjection::getJobId, Collectors.mapping(JobSavedRepository.JobDescriptionProjection::getDescription, Collectors.toList())));
    }

    private Map<Long, List<String>> loadRequirementsMap(List<Long> jobIds) {
        //1. Tải tất cả các yêu cầu cho danh sách ID công việc.
        List<JobSavedRepository.JobRequirementProjection> projections = jobSavedRepository.findRequirementsByJobIds(jobIds);
        //2. Nhóm các yêu cầu theo ID công việc.
        return projections.stream().collect(Collectors.groupingBy(JobSavedRepository.JobRequirementProjection::getJobId, Collectors.mapping(JobSavedRepository.JobRequirementProjection::getRequirement, Collectors.toList())));
    }

    private Map<Long, List<String>> loadBenefitsMap(List<Long> jobIds) {
        //1. Tải tất cả các quyền lợi cho danh sách ID công việc.
        List<JobSavedRepository.JobBenefitProjection> projections = jobSavedRepository.findBenefitsByJobIds(jobIds);
        //2. Nhóm các quyền lợi theo ID công việc.
        return projections.stream().collect(Collectors.groupingBy(JobSavedRepository.JobBenefitProjection::getJobId, Collectors.mapping(JobSavedRepository.JobBenefitProjection::getBenefit, Collectors.toList())));
    }

    private void formatDisplayFields(GetJobSaveResponse.SavedJobsData.SavedJobItem item) {
        //1. Định dạng các giá trị enum để hiển thị thân thiện với người dùng.
        item.setLocationDisplay(formatEnum(item.getLocation()));
        item.setJobTypeDisplay(formatEnum(item.getJobType()));
        item.setSalaryRangeDisplay(formatEnum(item.getSalaryRange()));
        item.setExperienceDisplay(formatEnum(item.getExperience()));
        item.setIndustryDisplay(formatEnum(item.getIndustry()));
        item.setStatusDisplay(formatEnum(item.getStatus()));
        //2. Định dạng các giá trị ngày tháng.
        if (item.getApplicationDeadline() != null) {
            item.setApplicationDeadlineFormatted(item.getApplicationDeadline().format(DateTimeFormatter.ofPattern("dd MMM, yyyy")));
        }
        if (item.getPostedDate() != null) {
            item.setPostedDateFormatted(item.getPostedDate().format(DateTimeFormatter.ofPattern("dd MMM, yyyy")));
        }
        if (item.getSavedAt() != null) {
            item.setSavedAtFormatted(item.getSavedAt().format(DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm")));
            item.setTimeAgoText(calculateTimeAgo(item.getSavedAt()));
        }
        //3. Tính toán các cờ trạng thái.
        item.setIsExpired(item.getStatus() == JobPostingStatus.EXPIRED);
        item.setIsOpen(item.getStatus() == JobPostingStatus.OPEN);
        item.setIsActive(item.getIsOpen() && item.getApplicationDeadline() != null && item.getApplicationDeadline().isAfter(LocalDate.now()));
        if (item.getMaxApplicants() != null && item.getCurrentApplicants() != null) {
            item.setCanAcceptMoreApplicants(item.getCurrentApplicants() < item.getMaxApplicants());
        }
        //4. Định dạng trạng thái ứng tuyển.
        if (item.getApplicationStatus() != null) {
            item.setApplicationStatusDisplay(formatEnum(item.getApplicationStatus()));
        }
        if (item.getAppliedAt() != null) {
            item.setAppliedAtFormatted(item.getAppliedAt().format(DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm")));
        }
    }

    private String formatEnum(Enum<?> enumValue) {
        //1. Nếu giá trị enum là null, trả về "N/A".
        if (enumValue == null) return "N/A";
        //2. Dựa vào loại enum, trả về chuỗi hiển thị tương ứng.
        return switch (enumValue.getClass().getSimpleName()) {
            case "LocationType" -> ((LocationType) enumValue).getDisplayName();
            case "JobType" -> ((JobType) enumValue).getDisplayName();
            case "SalaryRange" -> ((SalaryRange) enumValue).getDisplayName();
            case "ExperienceLevel" -> ((ExperienceLevel) enumValue).getDisplayName();
            case "IndustryType" -> ((IndustryType) enumValue).getDisplayName();
            case "JobPostingStatus" -> ((JobPostingStatus) enumValue).name();
            case "ApplicationStatus" -> ((ApplicationStatus) enumValue).name();
            default -> enumValue.toString();
        };
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        //1. Tính toán khoảng thời gian giữa thời điểm hiện tại và thời điểm được cung cấp.
        LocalDateTime now = LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(dateTime, now);
        //2. Trả về chuỗi định dạng "X days/hours/minutes ago".
        if (days == 0) {
            long hours = ChronoUnit.HOURS.between(dateTime, now);
            if (hours == 0) {
                long minutes = ChronoUnit.MINUTES.between(dateTime, now);
                return minutes + " minutes ago";
            }
            return hours + " hours ago";
        } else if (days == 1) {
            return "1 day ago";
        }
        return days + " days ago";
    }
}
