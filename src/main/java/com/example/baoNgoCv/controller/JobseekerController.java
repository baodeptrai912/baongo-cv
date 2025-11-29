package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.model.dto.*;
import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.dto.applicant.GetJobApplicantDetailResponse;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicationDetailResponse;
import com.example.baoNgoCv.model.dto.common.ApiResponse;

import com.example.baoNgoCv.model.dto.common.PasswordChangeRequest;
import com.example.baoNgoCv.model.dto.common.VerifyPasswordRequest;
import com.example.baoNgoCv.model.dto.company.GetCompanyDetailResponse;
import com.example.baoNgoCv.model.dto.jobposting.*;

import com.example.baoNgoCv.exception.JobseekerExceptionHandler;
import com.example.baoNgoCv.exception.jobseekerException.UserNotFoundException;
import com.example.baoNgoCv.model.dto.user.*;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.model.enums.ApplicationStatus;
import com.example.baoNgoCv.model.enums.JobPostingStatus;
import com.example.baoNgoCv.jpa.repository.*;
import com.example.baoNgoCv.jpa.projection.jobPosting.JobCardProjection;
import com.example.baoNgoCv.model.enums.Skill;
import com.example.baoNgoCv.model.session.PendingPasswordChange;
import com.example.baoNgoCv.service.domainService.*;
import com.example.baoNgoCv.service.securityService.PasswordChangeService;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.ExportService;
import com.example.baoNgoCv.service.utilityService.FileService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/jobseeker")
@Slf4j
@RequiredArgsConstructor
public class JobseekerController {

    private final EmailService emailService;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final JobSavedRepository jobSavedRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobSavedService jobSavedService;
    private final UserRepository userRepository;
    private final UserSettingsService userSettingsService;
    private final JobAlertService jobAlertService;
    private final UserService userService;
    private final EducationRepository educationRepository;
    private final JobExperienceRepository jobExperienceRepository;
    private final FileService fileService;
    private final JobPostingService jobPostingService;
    private final CompanyService companyService;
    private final ApplicantRepository applicantRepository;
    private final ApplicantService applicantService;
    private final JobseekerExceptionHandler jobseekerExceptionHandler;
    private final ExportService exportService;
    private final PasswordEncoder passwordEncoder;
    private final PendingPasswordChange pendingPasswordChange;
    private final PasswordChangeService passwordChangeService;


    @GetMapping("/profile")
    public String profilePage(Model model, @RequestParam(name = "notificationId", required = false) Long notificationId) {
        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }

        try {
            GetProfileResponse profileData = userService.getProfileData();

            model.addAttribute("profile", profileData);
            return "jobseeker/profile";

        } catch (UserNotFoundException e) {
            log.error("User not found in profile controller", e);
            return "status/404";
        } catch (Exception e) {
            log.error("Error in profile controller", e);
            return "status/500";
        }
    }

    @GetMapping("/profile-update")
    public String profileUpdateAction(@RequestParam(value = "idNoti", required = false) Long idNoti, Model model) {
        User user = userService.getCurrentUser();

        GetProfileUpdateResponse profileData = userService.getProfileUpdateData();

        model.addAttribute("profileData", profileData);

        return "jobseeker/profile-update";
    }

    @PostMapping("/education")
    public ResponseEntity<ApiResponse<PostEducationResponse>> createEducation(@Valid @RequestBody PostEducationRequest request) {

        PostEducationResponse response = userService.saveEducation(request);

        ApiResponse<PostEducationResponse> apiResponse = ApiResponse.success(response, "Education added successfully");

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/education/{id}")
    public ResponseEntity<ApiResponse<PutEducationResponse>> editEducation(@PathVariable Long id,
                                                                           @Valid @RequestBody PutEducationRequest request) {
        // Logging để debug
        log.info("Bắt đầu xử lý yêu cầu cập nhật học vấn với ID: {}", id);
        log.debug("Dữ liệu nhận được từ request: {}", request);

        PutEducationResponse response = userService.updateEducation(id, request);

        log.info("Cập nhật học vấn với ID: {} thành công.", response);
        return ResponseEntity.ok(ApiResponse.success(response, "Education information updated successfully."));
    }


    @DeleteMapping("/education/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> deleteEducation(@PathVariable Long id) {
        // ✅ Logic đã được chuyển vào service, controller chỉ việc gọi
        userService.deleteEducation(id);
        return ResponseEntity.ok(
                ApiResponse.success("Education deleted successfully.")
        );
    }


    @PostMapping("/job-ex-add")
    public String jobExAdd(@ModelAttribute Set<EducationUpdateDTO> educationUpdateDTOs) {


        return "redirect:/jobseeker/profile";
    }


    @GetMapping("/job-detail/{id}")
    public String jobDetail(@PathVariable("id") Long id, @RequestParam(value = "notificationId", required = false) Long notiId, Model model) {
        log.info("Getting job detail for ID: {}", id);
        try {

            if (notiId != null) {
                notificationService.markAsRead(notiId);
            }

            var dtoResponse = jobPostingService.getJobDetail(id);

            model.addAttribute("response", dtoResponse);

            return "jobseeker/job-detail";
        } catch (RuntimeException e) {
            log.error("Error getting job detail for ID: {}", id, e);
            return "status/500";
        }
    }

    @GetMapping("/company-detail/{id}")
    public String companyDetail(@PathVariable("id") Long id, Model model) {
        try {
            GetCompanyDetailResponse response = companyService.getCompanyDetailComplete(id);

            model.addAttribute("response", response);

            return "jobseeker/company-detail";
        } catch (RuntimeException e) {
            // [FIX] Ghi lại log lỗi để dễ dàng gỡ lỗi
            log.error("Error fetching company detail for ID: {}", id, e);
            return "status/500";
        }
    }


    private boolean arePeriodsOverlapping(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        LocalDate effectiveEnd1 = (end1 == null) ? LocalDate.MAX : end1;
        LocalDate effectiveEnd2 = (end2 == null) ? LocalDate.MAX : end2;
        boolean startsBeforeOrOnEnd2 = !start1.isAfter(effectiveEnd2); // start1 <= effectiveEnd2
        boolean endsAfterOrOnStart2 = !effectiveEnd1.isBefore(start2);  // effectiveEnd1 >= start2

        return startsBeforeOrOnEnd2 && endsAfterOrOnStart2;
    }

    @PostMapping("/job-experience")
    public ResponseEntity<ApiResponse<PostJobExperienceResponse>> createJobExperience(@RequestBody @Valid PostJobExperienceRequest request) {
        PostJobExperienceResponse response = userService.createJobExperienceForCurrentUser(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Work experience added successfully"));
    }

    @PutMapping("/job-experience/{id}")
    public ResponseEntity<ApiResponse<PutJobExperienceResponse>> updateJobExperience(@PathVariable Long id, @Valid @RequestBody PutJobExperienceRequest request) {
        log.info("Updating job experience ID: {}", id);
        PutJobExperienceResponse responseData = userService.updateJobExperience(id, request);

        return ResponseEntity.ok(ApiResponse.success(responseData, "Job experience updated successfully"));
    }


    @DeleteMapping("/job-experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobExperience(@PathVariable Long id) {
        userService.deleteJobExperience(id);
        return ResponseEntity.ok(
                ApiResponse.success("Work experience deleted successfully.")
        );
    }

    @GetMapping("/my-application")
    public String getMyApplication(Model model, @RequestParam(name = "notiId", required = false) Long notiId, @RequestParam(name = "highlight", required = false) Long highlightId) {  // ✅ Add highlight param

        User currentUser = userService.getCurrentUser();

        GetMyApplicantResponse response = userService.getMyApplicants(currentUser, notiId, highlightId);

        model.addAttribute("response", response);
        return "jobseeker/my-application";
    }

    /**
     * [NEW] API endpoint to fetch user's applications with filtering.
     * This is used for client-side rendering with loading effects.
     * @param status The application status to filter by (e.g., "pending", "rejected").
     * @return ApiResponse containing the GetMyApplicantResponse DTO.
     */
    @GetMapping("/api/my-applications")
    @ResponseBody
    public ResponseEntity<ApiResponse<GetMyApplicantResponse>> getMyApplicationsApi(
            @RequestParam(name = "status", required = false) String status) {

        User currentUser = userService.getCurrentUser();
        // We can reuse the existing service method. The highlight parameters are null here.
        GetMyApplicantResponse responseData = userService.getMyApplicants(currentUser, null, null);

        return ResponseEntity.ok(ApiResponse.success(responseData, "Applications fetched successfully."));
    }

    @GetMapping("/my-application/{applicantId}")
    public String getApplicationDetail(
            Model model,
            @PathVariable("applicantId") Long applicantId,
            @RequestParam(name = "notificationId", required = false) Long notificationId) {

        // 1. Lấy User đang đăng nhập (Jobseeker)
        // Đây là bước BẮT BUỘC để đảm bảo Jobseeker chỉ xem được hồ sơ của chính mình.
        if (notificationId != null) {
            log.info("Marking notification {} as read for application detail view.", notificationId);
            notificationService.markAsRead(notificationId);
        }

        User currentUser = userService.getCurrentUser();

        // 2. Gọi Service để lấy chi tiết hồ sơ
        // Service này phải kiểm tra applicantId có thuộc về currentUser.
        // Giả sử service này trả về một DTO chi tiết tương tự như của HR nhưng ở góc nhìn Jobseeker.
        GetMyApplicationDetailResponse detailResponse =
                applicantService.getApplicantDetailForJobseeker(currentUser, applicantId);

        // 3. QUAN TRỌNG: In thử ra console xem có null không
        System.out.println("DTO Data: " + detailResponse);

        // 3. Đưa vào Model và trả về template
        model.addAttribute("applicantDetail", detailResponse);
        model.addAttribute("user", currentUser);
        // Giả sử bạn tạo một template mới để hiển thị chi tiết 1 hồ sơ duy nhất
        return "jobseeker/my-application-detail";
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        System.out.println("File name received: " + fileName);
        Resource resource = (Resource) fileService.loadFileAsResource(fileName);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }


    @GetMapping("/apply-job/{id}")
    public String showApplyJob(@PathVariable("id") Long jobPostingId, Model model) {

        try {
            GetApplyJobResponse applyJobResponse = jobPostingService.getApplyJobData(jobPostingId);

            model.addAttribute("applyJob", applyJobResponse);

            return "jobseeker/apply-job";
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            return "status/500";
        }

    }

    @PostMapping("/apply-job/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse<PostApplyJobResponse>> handleApplicationForm(@PathVariable("id") Long jobPostingId, @Valid @ModelAttribute PostApplyJobRequest request) {

        PostApplyJobResponse result = userService.applyForJob(jobPostingId, request);

        return ResponseEntity.ok(ApiResponse.success(result, "Application submitted successfully!"));
    }

    @PostMapping("/personal-infor-update")
    public ResponseEntity<ApiResponse<PostPersonalInfoResponse>> updateProfile(@Valid @ModelAttribute PersonalInforUpdateDTO personalInforUpdateDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> fieldErrors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

            ApiResponse<PostPersonalInfoResponse> response = ApiResponse.validationError("Please check the highlighted fields and try again.", fieldErrors);
            return ResponseEntity.badRequest().body(response);
        }

        PostPersonalInfoResponse result = userService.updatePersonalInfo(personalInforUpdateDTO);

        ApiResponse<PostPersonalInfoResponse> response = ApiResponse.success(result, "Personal information updated successfully!");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/account-settings")
    public String accountSettingsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        System.out.println("[DEBUG] UserController.accountSettingsPage CALLED");
        if (userDetails != null) {
            String username = userDetails.getUsername();
            System.out.println("[DEBUG] Loading settings for user: " + username);
            try {
                UserSettings settings = userSettingsService.getCurrentUserSettings(username); // Hàm này sẽ lấy hoặc tạo mới UserSettings với giá trị mặc định

                String currentVisibility = settings.isProfilePublic() ? "PUBLIC" : "PRIVATE";
                model.addAttribute("currentProfileVisibility", currentVisibility);
                System.out.println("[DEBUG] Current profile visibility for " + username + ": " + currentVisibility);

                boolean currentEmailNotification = settings.isEmailOnApplicationUpdate();
                model.addAttribute("currentEmailNotificationSetting", currentEmailNotification);
                System.out.println("[DEBUG] Current email notification setting for " + username + ": " + currentEmailNotification);

            } catch (UsernameNotFoundException e) {
                System.err.println("[DEBUG] User not found when trying to load settings for account-settings page: " + username);

            } catch (Exception e) {
                System.err.println("[DEBUG] Error loading user settings for account-settings page: " + e.getMessage());

            }
        } else {
            System.out.println("[DEBUG] UserDetails is null in accountSettingsPage. User might not be authenticated.");

        }
        return "jobseeker/account-settings";
    }

    /**
     * Cập nhật danh sách liên kết mạng xã hội của người dùng.
     *
     * @param socialLinksRequest Danh sách các liên kết mới. {@code @Valid} sẽ kiểm tra từng mục.
     * @return {@link ApiResponse} với thông báo thành công.
     */
    @PostMapping("/update-social-links")
    public ResponseEntity<ApiResponse<Void>> updateSocialLinks(
            @Valid
            @RequestBody List<PostSocialLinkRequest> socialLinksRequest
    ) {

        // 1. Chuyển request đến service để xử lý (mọi exception sẽ được GlobalExceptionHandler bắt)
        userService.updateSocialLinks(socialLinksRequest);

        // 2. Trả về response thành công
        return ResponseEntity.ok(
                ApiResponse.success("Social links updated successfully!")
        );
    }


    /**
     * Endpoint để cập nhật hoặc thay thế toàn bộ danh sách kỹ năng của người dùng.
     *
     * @param skillsRequest Danh sách các kỹ năng (dưới dạng chuỗi enum) được gửi trong body của request.
     *                      Spring Boot sẽ tự động chuyển đổi mảng JSON ["JAVA", "SQL"] thành List<Skill>.
     * @return Một {@link ResponseEntity} chứa {@link ApiResponse} với thông báo thành công.
     */
    @PostMapping("/update-skills")
    public ResponseEntity<PostUpdateSkillResponse> updateSkills(@RequestBody List<Skill> skillsRequest) {
        log.info("🚀 [SKILLS_UPDATE] Starting skills update");
        log.debug("📊 [SKILLS_UPDATE] Request payload: {}", skillsRequest);
        log.info("📊 [SKILLS_UPDATE] Received {} skills: {}",
                skillsRequest != null ? skillsRequest.size() : 0, skillsRequest);

        // Bước 1: Chuyển tiếp danh sách kỹ năng đến tầng service
        PostUpdateSkillResponse response = userService.updateSkills(skillsRequest);


        log.info("✅ [SKILLS_UPDATE] Skills updated successfully");

        // Bước 2: Trả về response HTTP 200 OK
        return ResponseEntity.ok(
                response
        );

    }

    @PostMapping("/save-job/{jobPostingId}")
    public ResponseEntity<ApiResponse<Void>> saveJob(@PathVariable Long jobPostingId) {

        jobPostingService.saveJob(jobPostingId);

        ApiResponse<Void> response = ApiResponse.success("Job saved successfully!");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/unsave-job/{jobPostingId}")
    public ResponseEntity<ApiResponse<Void>> unSaveJob(@PathVariable Long jobPostingId) {
        jobPostingService.unSaveJob(jobPostingId);
        return ResponseEntity.ok(ApiResponse.success("Job unsaved successfully!"));
    }

    @GetMapping("/job-save")
    public String jobSaved(@ModelAttribute GetJobSaveRequest request,
                           @RequestParam(name = "notiId", required = false) Long notificationId,
                           @RequestParam(name = "highlight", required = false) Long highlightId,
                           @PageableDefault(size = 6) Pageable defaultPageable, Model model) {

        if (request.getPage() == null) {
            request.setPage(defaultPageable.getPageNumber());
        }
        if (request.getSize() == null) {
            request.setSize(defaultPageable.getPageSize());
        }

        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }

        GetJobSaveResponse response = jobPostingService.getSavedJobsPage(request);
        response.setHighlightJobId(highlightId);
        model.addAttribute("response", response);

        return "jobseeker/job-save";
    }


    @PostMapping("/job-following")
    public ResponseEntity<ApiResponse<Void>> followCompany(@RequestParam Long companyId, @RequestParam boolean follow) {
        // ✨ [REFACTOR] Standardize API response using ApiResponse
        // The try-catch block is removed. Exceptions will be handled by JobseekerExceptionHandler.
        if (follow) {
            userService.addFollower(companyId);
            return ResponseEntity.ok(
                    ApiResponse.success("Company followed successfully")
            );
        } else {
            userService.removeFollower(companyId);
            return ResponseEntity.ok(
                    ApiResponse.success("Company unfollowed successfully")
            );
        }
    }


    /**
     * [NEW] Endpoint cho phép Job Seeker rút đơn ứng tuyển.
     * @param id ID của đơn ứng tuyển (Applicant ID).
     * @return ApiResponse cho biết hành động đã thành công.
     */
    @PostMapping("/application/{id}/withdraw")
    public ResponseEntity<ApiResponse<String>> withdrawApplication(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User currentUser = (User) authentication.getPrincipal();

        applicantService.withdrawApplication(id, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("The application has been successfully withdrawn. The employer has been notified !"));
    }


    @GetMapping("/job-search")
    public String searchJobs(@Valid @ModelAttribute GetJobSearchRequest searchRequest, BindingResult bindingResult, Model model) {

        //1. Handle validation errors - nếu có lỗi validation, trả về default results
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors: {}", bindingResult.getAllErrors());
            // Return form with errors hoặc default results
            Page<JobCardProjection> defaultJobs = jobPostingService.searchJobPostings(GetJobSearchRequest.withDefaults());
            GetJobSearchResponse response = GetJobSearchResponse.from(GetJobSearchRequest.withDefaults(), defaultJobs);
            model.addAttribute("searchResponse", response);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "jobseeker/job-search";
        }

        //2. Check if user has provided any search criteria - kiểm tra có filter nào được áp dụng không
        boolean hasSearchCriteria = searchRequest.keyword() != null && !searchRequest.keyword().trim().isEmpty() || (searchRequest.locations() != null && !searchRequest.locations().isEmpty()) || (searchRequest.experiences() != null && !searchRequest.experiences().isEmpty()) || (searchRequest.salaryRanges() != null && !searchRequest.salaryRanges().isEmpty()) || (searchRequest.jobTypes() != null && !searchRequest.jobTypes().isEmpty()) || (searchRequest.industries() != null && !searchRequest.industries().isEmpty()) || searchRequest.postedAfter() != null || searchRequest.deadlineBefore() != null;

        //3. Normalize and validate request data - clean up và set default values
        GetJobSearchRequest normalizedRequest = searchRequest.validateAndNormalize();

        //4. Handle initial page load (no search criteria) - lần đầu vào trang, hiển thị jobs mặc định
        if (!hasSearchCriteria) {
            log.info("Initial page load - showing default jobs");
            Page<JobCardProjection> defaultJobs = jobPostingService.searchJobPostings(normalizedRequest);
            GetJobSearchResponse response = GetJobSearchResponse.from(normalizedRequest, defaultJobs);
            model.addAttribute("searchResponse", response);
            return "jobseeker/job-search";
        }

        //5. Handle filtered search with criteria - thực hiện tìm kiếm với điều kiện filter
        log.info("Filtered search with criteria");
        Page<JobCardProjection> jobResults = jobPostingService.searchJobPostings(normalizedRequest);
        GetJobSearchResponse response = GetJobSearchResponse.from(normalizedRequest, jobResults);
        model.addAttribute("searchResponse", response);

        //6. Return view template - trả về trang HTML với kết quả tìm kiếm
        return "jobseeker/job-search";
    }

    @GetMapping("/job-search-api")
    public ResponseEntity<GetJobSearchResponse> searchJobsApi(
            @Valid @ModelAttribute GetJobSearchRequest searchRequest,
            HttpServletRequest httpRequest
    ) {


        GetJobSearchRequest normalizedRequest = searchRequest.validateAndNormalize();
        log.info("✅ NORMALIZED REQUEST - page: {}, size: {}", normalizedRequest.page(), normalizedRequest.size());

        Page<JobCardProjection> jobResults = jobPostingService.searchJobPostings(normalizedRequest);

        GetJobSearchResponse response = GetJobSearchResponse.from(normalizedRequest, jobResults);


        log.info("========== API CALL COMPLETED ==========\n");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/job-search-live")
    public ResponseEntity<Map<String, Object>> searchJobsLive(@RequestParam(value = "keyword") String keyword) {

        Map<String, Object> response = new HashMap<>();

        // Tìm kiếm danh sách công việc theo từ khóa
        List<JobPosting> listJobPosting = jobPostingRepository.findByTitle(keyword);

        // Chuyển đổi danh sách JobPosting thành danh sách JobPostingSearchDTO
        List<JobPostingSearchDTO> jobDTOs = listJobPosting.stream().map(job -> new JobPostingSearchDTO(job.getId(), job.getTitle(), job.getCompany().getName(),  // Giả sử job có thuộc tính company
                job.getCompany().getCompanyLogo(), job.getLocation(), job.getExperience(), job.getSalaryRange())).collect(Collectors.toList());

        response.put("listRelevant", jobDTOs);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/notification-settings")
    public ResponseEntity<Map<String, Object>> updateEmailNotificationSettings(@RequestBody NotificationSettingsDto settingsDto, Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "User is not authenticated.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        try {
            String username = authentication.getName();
            userService.updateNotificationSettings(username, settingsDto.emailOnApplicationUpdate());

            response.put("success", true);
            response.put("message", "Notification settings updated successfully.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            log.error("Error updating notification settings for user: {}", authentication.getName(), e);

            response.put("success", false);
            response.put("message", "An unexpected error occurred while updating settings.");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * ✅ NEW: Endpoint to update user's privacy settings.
     * Handles PUT requests to change profile visibility (public/private).
     *
     * @param settingsDto    The request body containing the new privacy setting.
     * @param authentication The current user's authentication details.
     * @return An ApiResponse indicating success or failure.
     */
    @PutMapping("/privacy")
    public ResponseEntity<ApiResponse<Void>> updatePrivacySettings(
            @RequestBody PrivacySettingsDto settingsDto,
            Authentication authentication) {

        // 1. Get username from authenticated principal
        String username = authentication.getName();

        // 2. ✅ IMPROVED: Unpack DTO in the controller layer
        //    The controller is responsible for handling web-layer data (DTOs).
        boolean isPublic = settingsDto.isProfilePublic();

        // 3. Delegate pure business logic to the service layer
        userService.updatePrivacySettings(username, isPublic);

        // 4. Return a standardized success response
        return ResponseEntity.ok(ApiResponse.success("Privacy settings updated successfully."));
    }

    @GetMapping("/job-alert")
    public String homePage(Model model) {
        User currentUser = userService.getCurrentUser();

        List<JobAlert> alerts = jobAlertService.getUserJobAlerts(currentUser);

        model.addAttribute("alerts", alerts);
        model.addAttribute("maxAlerts", 10);
        model.addAttribute("currentCount", alerts.size());
        log.info("Loaded {} job alerts for user {}", alerts.size(), currentUser.getContactInfo().getEmail());

        return "jobseeker/job-alert";
    }

    @PostMapping("/job-alert")
    public ResponseEntity<ApiResponse<JobAlertResponse>> createJobAlert(@Valid @RequestBody JobAlertDTO dto) {


        JobAlertResponse response = jobAlertService.createJobAlert(dto);


        ApiResponse<JobAlertResponse> apiResponse = ApiResponse.success(response, "Job alert created successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/job-alert/{id}")
    public ResponseEntity<ApiResponse<JobAlertResponse>> updateJobAlert(@PathVariable Long id, @Valid @RequestBody JobAlertDTO dto) {

        JobAlertResponse response = jobAlertService.updateJobAlert(id, dto);

        ApiResponse<JobAlertResponse> apiResponse = ApiResponse.success(response, "Job alert updated successfully");

        return ResponseEntity.ok(apiResponse);
    }


    @DeleteMapping("/job-alert/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobAlert(@PathVariable Long id) {

        jobAlertService.deleteJobAlert(id);

        return ResponseEntity.ok(ApiResponse.success("Job alert deleted successfully"));

    }


    @PostMapping("/delete-account/verify-password")
    @ResponseBody
    public ResponseEntity<PostVerifyPasswordForDeletionResponse> verifyPasswordForDeletion(@Valid @RequestBody VerifyPasswordRequest request) {
        //1 Ủy quyền cho tầng Service
        PostVerifyPasswordForDeletionResponse response = userService.verifyPasswordToDeleteAccount(request);

        //2 Xây dựng và trả về phản hồi HTTP 200 OK
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-account/finalize")
    public ResponseEntity<ApiResponse<PostDeleteAccountFinalizeResponse>> finalizeAccountDeletion(
            @RequestBody Map<String, String> requestBody) {
        String code = requestBody.get("verificationCode");
        // 1. Thực hiện logic xóa tài khoản
        PostDeleteAccountFinalizeResponse responseDto = userService.finalizeAccountDeletion(code);

        // 2. Tạo ApiResponse chuẩn từ DTO trả về
        ApiResponse<PostDeleteAccountFinalizeResponse> apiResponse = ApiResponse.success(
                responseDto,
                "Your account has been deleted permanently!"
        );

        // 3. Trả về response
        return ResponseEntity.ok(apiResponse);
    }


    @GetMapping("/export/applications")
    @ResponseBody
    public ResponseEntity<Resource> exportApplications(
            @RequestParam(defaultValue = "csv") String format) {

        ExportResult exportResult = exportService.exportMyApplications(format);

        ByteArrayResource resource = new ByteArrayResource(exportResult.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportResult.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + exportResult.getFileName() + "\"")
                .body(resource);


    }

    // =================================================================
    // CHANGE PASSWORD ENDPOINTS
    // =================================================================

    /**
     * Handles the initial request to change a user's password.
     * Validates the current password, and if correct, sends a verification code to the user's email.
     * The new password is temporarily stored in the session pending verification.
     *
     * @param request The request containing current and new passwords.
     * @return An ApiResponse indicating success or failure.
     */
    @PostMapping("/account-settings/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) throws MessagingException {

        // 1. Lấy user hiện tại
        User currentUser = userService.getCurrentUser();

        // 2. Giao hết cho service xử lý
        passwordChangeService.initiatePasswordChange(currentUser, request);

        // 3. Trả về response
        Map<String, String> data = Map.of("email", currentUser.getContactInfo().getEmail());
        return ResponseEntity.ok(ApiResponse.success(data, "Verification code sent to your email."));
    }


    /**
     * Verifies the email code to finalize the password change.
     *
     * @param code The verification code submitted by the user.
     * @return An ApiResponse indicating success or failure.
     */
    @PostMapping("/verify-password-change")
    public ResponseEntity<ApiResponse<Void>> verifyPasswordChange(@RequestParam("code") String code) {
        // 1. Lấy thông tin người dùng hiện tại
        User currentUser = userService.getCurrentUser();

        // 2. Khởi tạo service

        passwordChangeService.verifyAndFinalizePasswordChange(currentUser, code);

        // 3. Nếu không có exception nào, nghĩa là đã thành công
        log.info("Password for user {} changed successfully.", currentUser.getContactInfo().getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully! Please log in again."));
    }

    /**
     * Resends the password change verification code.
     * This is a placeholder and should be implemented with proper rate-limiting.
     */
    @PostMapping("/resend-password-code")
    public ResponseEntity<ApiResponse<Void>> resendPasswordCode() throws MessagingException {
        User currentUser = userService.getCurrentUser();

        // Gọi đúng service
        // Mọi lỗi (rate limit, chưa khởi tạo...) sẽ được service xử lý và ném exception
        passwordChangeService.resendPasswordChangeCode(currentUser);

        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent."));
    }
}
