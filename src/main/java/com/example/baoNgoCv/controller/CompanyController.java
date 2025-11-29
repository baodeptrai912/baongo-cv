package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.exception.companyException.InvalidPasswordChangeException;
import com.example.baoNgoCv.exception.jobseekerException.UserNotFoundException;
import com.example.baoNgoCv.model.dto.JobPostingMetricsDTO;
import com.example.baoNgoCv.model.dto.company.ApplicantFilterRequest;
import com.example.baoNgoCv.model.dto.company.ApplicantViewingDto;
import com.example.baoNgoCv.model.dto.NotificationSettingsRequestDTO;
import com.example.baoNgoCv.model.dto.applicant.GetJobApplicantDetailResponse;
import com.example.baoNgoCv.model.dto.applicant.PostScheduleInterviewRequest;
import com.example.baoNgoCv.model.dto.common.ApiResponse;
import com.example.baoNgoCv.model.dto.common.PasswordChangeRequest;
import com.example.baoNgoCv.model.dto.common.VerifyPasswordRequest;

import com.example.baoNgoCv.model.dto.company.*;

import com.example.baoNgoCv.model.dto.jobposting.GetJobResponse;
import com.example.baoNgoCv.model.dto.jobposting.PostJobRequest;
import com.example.baoNgoCv.model.dto.jobposting.PostJobResponse;
import com.example.baoNgoCv.model.dto.payment.GetTransactionHistoryResponse;
import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.dto.user.UserProfileView;
import com.example.baoNgoCv.model.enums.*;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.jpa.repository.*;
import com.example.baoNgoCv.jpa.projection.company.CompanyDTO;
import com.example.baoNgoCv.service.domainService.*;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import com.example.baoNgoCv.service.utilityService.ExportService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/company")
@Slf4j
@RequiredArgsConstructor
public class CompanyController {
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
    private final CompanyRepository companyRepository;
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JobPostingRepository jobPostingRepository;
    private final ApplicantRepository applicantRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService;
    private final UserService userService;
    private final EmailService emailService;
    private final PermissionRepository permissionRepository;
    private final FileService fileService;
    private final JobPostingService jobPostingService;
    private final ApplicantService applicantService;
    private final ApplicationReviewRepository applicationReviewRepository;
    private final TransactionService transactionService;
    private final ExportService exportService;


    // 1. Endpoint trả về View (HTML) cho lần load đầu tiên
    @GetMapping("/companies")
    public String listCompanies(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "score") String sort,
                                @RequestParam(required = false) String keyword,
                                // SỬA: Đổi thành List để nhận được nhiều checkbox
                                @RequestParam(required = false) List<IndustryType> industry,
                                @RequestParam(required = false) LocationType location) {

        // Gọi Service (Service cũng phải sửa để nhận List, xem bên dưới)
        GetCompaniesResponse response = companyService.getCompaniesData(page, sort, keyword, industry, location);

        model.addAttribute("data", response);
        return "/main/company-list";
    }

    @GetMapping("/api/companies")
    @ResponseBody
    public ResponseEntity<GetCompaniesResponse> listCompaniesApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "score") String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<IndustryType> industry,
            @RequestParam(required = false) LocationType location) {

        GetCompaniesResponse response = companyService.getCompaniesData(page, sort, keyword, industry, location);

        // BẮT ĐẦU BLOCK LOGGING
        if (log.isInfoEnabled()) {
            log.info("================== API FILTERING CHECK ==================");
            log.info("Request Params | Page: {}, Sort: {}, Keyword: '{}', Industries: {}, Location: {}",
                    page, sort, keyword, industry, location);
            log.info(String.valueOf(response));

            log.info("=======================================================");
        }
        // KẾT THÚC BLOCK LOGGING

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public String getProfile(Model model, @RequestParam(required = false) Long notificationId) {
        Company currentCompany = userService.getCurrentCompany();
        GetCompanyProfileResponse response = companyService.getCompanyProfile(currentCompany.getId());
        model.addAttribute("response", response);
        model.addAttribute("allTiers", AccountTier.values());

        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }

        return "company/company-profile";
    }

    @GetMapping("/account-settings")
    public String getAccountSettings(Model model) {
        Company currentCompany = userService.getCurrentCompany();
        if (currentCompany == null) {
            return "redirect:/user/login";
        }

        boolean currentEmailSetting = Optional.ofNullable(currentCompany.getCompanySetting())
                .map(CompanySetting::isEmailOnNewApplicant)
                .orElse(true);

        model.addAttribute("currentEmailNotificationSetting", currentEmailSetting);
        return "company/account-settings";
    }

    @GetMapping("/profile-update")
    public String getProfileUpdate(Model model, Authentication auth) {
        Company currentCompany = (Company) auth.getPrincipal();
        GetProfileUpdateResponse getProfileUpdateResponse = companyService.getProfileUpdateResponse(currentCompany.getId());

        model.addAttribute("currentCompany", getProfileUpdateResponse);

        return "/company/company-profile-update";
    }

    @PutMapping(value = "/information", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateInformation(
            Authentication auth,
            @Valid @ModelAttribute PutInformationRequest request
    ) {
        Long companyId = ((Company) auth.getPrincipal()).getId();

        log.info("📥 Updating company profile - companyId: {}, request: {}", companyId, request);

        PutInformationResponse response = companyService.updateCompanyProfile(companyId, request);

        log.info("📤 Company profile updated successfully - companyId: {}, response: {}", companyId, response);

        return ResponseEntity.ok(
                ApiResponse.success(response.toMap(), "Company profile updated successfully")
        );


    }

    @PutMapping("/contact")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateContact(
            Authentication auth,
            @Valid @RequestBody PutContactRequest request) {
        Long companyId = ((Company) auth.getPrincipal()).getId();
        log.info("📥 Updating company contact - companyId: {}, request: {}", companyId, request);

        PutContactResponse response = companyService.updateCompanyContact(companyId, request);

        log.info("📤 Company contact updated successfully - companyId: {}, response: {}", companyId, response);

        return ResponseEntity.ok(ApiResponse.success(response.toMap(), "Contact information updated successfully"));
    }

    @GetMapping("/post-a-job")
    public String postAJob(Model model, Authentication auth) {
        String name = auth.getName();
        GetJobResponse company = jobPostingService.getPostAJobPage(auth);
        List<IndustryType> industries = Arrays.asList(IndustryType.values());

        model.addAttribute("company", company);
        model.addAttribute("industryList", industries);
        return "/company/post-a-job";
    }

    @GetMapping("/jobposting-managing")
    public String manageJobPosting(@PageableDefault(size = 10, sort = "posted_date", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(name = "notificationId", required = false) Long notificationId, Model model) {

        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }

        GetJobpostingManagingResponse response = jobPostingService.getJobPostingManagement(pageable);
        log.info("Job Posting Management Response DTO: {}", response);
        model.addAttribute("response", response);
        return "company/jobposting-managing";
    }

    /**
     * [NEW] API Endpoint to get job posting management data as JSON.
     * This serves client-side pagination, allowing the frontend to fetch
     * only the necessary data without a full page reload.
     *
     * @param pageable Pagination information (page, size, sort) from the request.
     * @return ResponseEntity containing the GetJobpostingManagingResponse DTO.
     */
    @GetMapping("/api/jobposting-managing")
    @ResponseBody
    public ResponseEntity<GetJobpostingManagingResponse> getManageJobPostingApi(
            @PageableDefault(size = 10, sort = "posted_date", direction = Sort.Direction.DESC) Pageable pageable) {

        GetJobpostingManagingResponse response = jobPostingService.getJobPostingManagement(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard-metrics")
    public ResponseEntity<?> getDashboardMetrics(Authentication authentication) {

        log.info("Request received for dashboard metrics.");

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthenticated access attempt to /dashboard-metrics.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication is required.");
        }

        String userEmail = authentication.getName();

        log.info("Metrics requested by user: {}", userEmail);

        try {
            log.debug("Attempting to find company with email: {}", userEmail);
            Optional<Company> companyOpt = companyService.findByUserName(userEmail);

            if (companyOpt.isEmpty()) {

                log.warn("No company found for authenticated user: {}", userEmail);

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Authenticated user is not associated with any company.");
            }

            Company company = companyOpt.get();
            Long currentCompanyId = company.getId();

            log.debug("Company found. ID: {}, Name: {}", currentCompanyId, company.getName());

            log.debug("Calculating metrics for company ID: {}", currentCompanyId);
            JobPostingMetricsDTO metrics = jobPostingService.calculateJobPostingMetrics(currentCompanyId);

            log.debug("Metrics calculated successfully: {}", metrics.toString());

            log.info("Successfully returned metrics for user: {}", userEmail);
            return ResponseEntity.ok(metrics);

        } catch (Exception e) {

            log.error("An unexpected error occurred while calculating metrics for user: {}", userEmail, e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An internal server error occurred.");
        }
    }


    @PostMapping("/post-a-job")
    public ResponseEntity<ApiResponse<PostJobResponse>> handlePostAJob(@Valid @RequestBody PostJobRequest postJobRequest, Authentication auth) {
        Company currentCompany = (Company) auth.getPrincipal();

        PostJobResponse dto = jobPostingService.publishJobPosting(postJobRequest, currentCompany.getId());

        ApiResponse<PostJobResponse> response = ApiResponse.success(dto, "Your job posting has been submitted for review!");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/job-posting/{id}")
    public ResponseEntity<ApiResponse<PutJobPostingResponse>> updateJobPosting(@PathVariable Long id, @Valid @RequestBody PutJobPostingRequest request) {
        PutJobPostingResponse response = jobPostingService.updateJobPosting(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Job posting updated successfully."));
    }

    @DeleteMapping("/job-posting/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(@PathVariable Long id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Job posting deleted successfully."));
    }

    @PutMapping("/update-job-posting-status/{jobPostingId}/{status}")
    public ResponseEntity<Map<String, Object>> updateJobStatus(@PathVariable Long jobPostingId, @PathVariable String status) {

        try {

            JobPostingStatus jobPostingStatus = JobPostingStatus.valueOf(status.toUpperCase());

            jobPostingService.updateJobPostingStatus(jobPostingId, jobPostingStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job status updated successfully.");
            response.put("status", status.toUpperCase());

            response.put("id", jobPostingId);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (IllegalArgumentException e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid job status value.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update job status.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @GetMapping("/job-application-detail/{id}")
    public String jobApplicationDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "notificationId", required = false) Long notificationId,
            Model model) {

        // 1. Xử lý Notification (nếu bấm từ thông báo vào)
        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }

        // 2. Gọi Service: Lấy chi tiết + Tự động đổi status PENDING -> REVIEWING
        // Trả về DTO thay vì Entity
        GetJobApplicantDetailResponse applicantDto = applicantService.getApplicantDetail(id);

        // 3. Đẩy dữ liệu sang View
        model.addAttribute("applicant", applicantDto);

        // (Optional) Nếu bạn muốn check Deleted riêng, nhưng tốt nhất nên nhét vào DTO
        // model.addAttribute("isDeleted", applicantDto.isDeleted());

        return "company/job-application-detail";
    }

    /**
     * Endpoint để Company xem hồ sơ công khai của một Job Seeker.
     * Endpoint này sẽ kiểm tra cài đặt riêng tư của người dùng trước khi hiển thị.
     *
     * @param userId ID của người dùng (Job Seeker) cần xem.
     * @param model  Model để truyền dữ liệu tới view.
     * @return Tên của template sẽ được render.
     */
    @GetMapping("/job-application-detail/user-profile/{userId}")
    public String viewJobseekerProfile(@PathVariable("userId") Long userId, Model model) {

        // 1. [REFACTORED] Gọi service để lấy DTO đã được chuẩn bị sẵn
        UserProfileView userProfile = userService.getProfileForViewing(userId);

        // 2. Truyền DTO duy nhất này sang cho view
        // View sẽ sử dụng cờ 'isPrivate' bên trong DTO để hiển thị giao diện phù hợp.
        model.addAttribute("userProfile", userProfile);


        return "company/user-profile";
    }


    @PostMapping("/review/submit")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> submitReview(@Valid @RequestBody PostReviewRequest request) {
        log.info("📥 [API] Received review for applicantId: {}, score: {}, note: '{}'",
                request.applicantId(), request.score(), request.note());

        applicantService.saveReview(request.applicantId(), request.score(), request.note());

        return ResponseEntity.ok(ApiResponse.success("Evaluation saved successfully!"));
    }



    @PostMapping("/job-application/schedule-interview")
    @ResponseBody
    public void scheduleInterview(@Valid @RequestBody PostScheduleInterviewRequest request) {
        // Không cần try-catch! GlobalExceptionHandler sẽ xử lý khi có ApplicantStatusTransitionException.

        applicantService.scheduleInterviewAndSendEmail(
                request.applicantId(),
                request.subject(),
                request.content()
        );

        // Không cần trả về gì cả (void)
    }

    @PostMapping("/job-application/shortlist/{applicantId}")
    @ResponseBody
    public void approveApplicant(@PathVariable Long applicantId) {

        applicantService.approveApplicant(applicantId);


    }

    @PostMapping("/job-application/reject/{applicantId}")
    @ResponseBody
    public void rejectApplicant(@PathVariable Long applicantId) {
        applicantService.rejectApplicant(applicantId);

    }

    @GetMapping("/job-view-applicants")
    public String applicantViewing(@ModelAttribute ApplicantFilterRequest filter, @RequestParam(name = "notificationId", required = false) Long notificationId, Model model) {

        // [NEW] Mark notification as read if ID is present in URL
        if (notificationId != null) {
            notificationService.markAsRead(notificationId);
        }
        
        // 1. Check quyền đăng nhập
        Company currentCompany = userService.getCurrentCompany(); // Service đã xử lý null

        // 2. Gọi Service lấy toàn bộ cục dữ liệu đã đóng gói
        ApplicantViewingDto responseData = applicantService.getApplicantViewingData(currentCompany, filter);

        log.debug("Fetched applicant data: {}", responseData);

        // 3. Kiểm tra nếu không có job nào

        // 4. Đẩy ra View
        model.addAttribute("data", responseData);
        model.addAttribute("filter", filter);

        // Cập nhật lại filter ID để đồng bộ với logic mặc định trong service
        if (filter.getJobId() == null && responseData.currentJob() != null) {
            filter.setJobId(responseData.currentJob().id());
        }

        return "company/applicant-viewing";
    }

    /**
     * API endpoint để lấy dữ liệu ứng viên dưới dạng JSON cho client-side rendering.
     *
     * @param filter Filter request chứa jobId và status.
     * @return ResponseEntity chứa ApplicantViewingDto hoặc lỗi 401 nếu chưa đăng nhập.
     */
    @GetMapping(value = "/api/job-view-applicants")
    @ResponseBody
    public ResponseEntity<ApplicantViewingDto> getApplicantsJson(
            @ModelAttribute ApplicantFilterRequest filter) {

        Company currentCompany = userService.getCurrentCompany();
        if (currentCompany == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ApplicantViewingDto responseData =
                applicantService.getApplicantViewingData(currentCompany, filter);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/job-application/bulk-shortlist")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> bulkShortlist(@RequestBody Map<String, List<Long>> payload) {
        // 1. Lấy danh sách ID từ payload
        List<Long> applicantIds = payload.get("applicantIds");
        if (applicantIds == null || applicantIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_INPUT", "Applicant IDs cannot be empty."));
        }

        // 2. Gọi service để xử lý hàng loạt
        applicantService.bulkShortlist(applicantIds);

        // 3. Trả về thành công
        return ResponseEntity.ok(ApiResponse.success(String.format("Successfully shortlisted %d applicants.", applicantIds.size())));
    }

    @PostMapping("/job-application/bulk-reject")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> bulkReject(@RequestBody Map<String, List<Long>> payload) {
        List<Long> applicantIds = payload.get("applicantIds");
        if (applicantIds == null || applicantIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_INPUT", "Applicant IDs cannot be empty."));
        }
        applicantService.bulkReject(applicantIds);
        return ResponseEntity.ok(ApiResponse.success(String.format("Successfully rejected %d applicants.", applicantIds.size())));
    }

    @PostMapping("/job-application/bulk-schedule-interview")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> bulkScheduleInterview(@RequestBody PostBulkScheduleInterviewRequest request) {
        List<Long> applicantIds = request.getApplicantIds();
        if (applicantIds == null || applicantIds.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("INVALID_INPUT", "Applicant IDs cannot be empty."));
        }

        applicantService.bulkScheduleInterview(
                applicantIds,
                request.getSubject(),
                request.getContent()
        );

        return ResponseEntity.ok(ApiResponse.success(String.format("Successfully sent interview invitations to %d applicants.", applicantIds.size())));
    }
    @GetMapping("/getFollower/{id}")
    public ResponseEntity<Map<String, Object>> getFollower(@PathVariable Long id, Model model) {

        Company currentCompany = userService.getCurrentCompany();

        Set<User> listFollower = currentCompany.getFollowers();

        List<Map<String, Object>> followersData = new ArrayList<>();
        for (User follower : listFollower) {
            Map<String, Object> followerInfo = new HashMap<>();
            followerInfo.put("id", follower.getId());
            followerInfo.put("name", follower.getPersonalInfo().getFullName());
            followerInfo.put("avatarUrl", follower.getProfilePicture());
            followersData.add(followerInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("followers", followersData);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public String verifyCompanyEmail(@RequestParam("verificationCode") String userEnteredCode, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        return null;
    }


    @PostMapping("/account/password-changing/request")
    @ResponseBody
    public ResponseEntity<?> requestPasswordChange(@Valid @RequestBody PasswordChangeRequest passwordChangeDTO, HttpSession session) throws MessagingException {

        PasswordChangeInitResponse result = companyService.initiatePasswordChange(
                passwordChangeDTO.currentPassword(),
                passwordChangeDTO.newPassword()
        );

        session.setAttribute("pending_password_change_email_company", result.email());
        session.setAttribute("verification_code_timestamp_company", new Date().getTime());
        // Lưu mật khẩu mới vào session để sử dụng ở bước cuối cùng
        session.setAttribute("new_company_password", passwordChangeDTO.newPassword());

        return ResponseEntity.ok(ApiResponse.success(result, "Verification email sent successfully!"));

    }

    @PutMapping("/account/password-changing/final")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> finalizePasswordChange(@Valid @RequestBody Map<String, String> requestBody, HttpSession session) { // Đổi tên phương thức để rõ ràng hơn
        String pendingEmail = (String) session.getAttribute("pending_password_change_email_company");
        String newPassword = (String) session.getAttribute("new_company_password");
        String code = requestBody.get("emailVerificationCode");
        try {
            // 1. Ủy thác cho Service
            companyService.finalizePasswordChange(code, pendingEmail, newPassword);

            // 2. Dọn dẹp session
            session.removeAttribute("pending_password_change_email_company");
            session.removeAttribute("verification_code_timestamp_company");
            session.removeAttribute("new_company_password");

            return ResponseEntity.ok(ApiResponse.success("Password has been updated successfully!"));

        } catch (InvalidPasswordChangeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.validationError("Validation failed", e.getErrors()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("SESSION_ERROR", e.getMessage()));
        }
    }

    @GetMapping("/account/send-email-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendEmailCode(HttpSession session) {
//        Map<String, Object> response = new HashMap<>();
//        String pendingEmail = (String) session.getAttribute("pending_password_change_email_company");
//
//        if (pendingEmail == null) {
//            response.put("success", false);
//            response.put("message", "No active password change process found. Please start over.");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//
//        try {
//            String verificationCode = emailService.generateVerificationCode();
//
//            emailService.sendVerificationCode(pendingEmail, verificationCode);
//
//            emailService.storeVerificationCode(pendingEmail, verificationCode);
//
//            session.setAttribute("verification_code_timestamp_company", new Date().getTime()); // Reset timestamp
//
//            response.put("success", true);
//            response.put("message", "A new verification code has been sent to your email.");
//            return ResponseEntity.ok(response);
//        } catch (MessagingException e) {
//            log.error("Failed to resend verification email to {}: {}", pendingEmail, e.getMessage());
//            response.put("success", false);
//            response.put("message", "Could not send verification email.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
        return null;
    }

    @PostMapping("/notification-settings")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(@AuthenticationPrincipal Company currentCompanyDetails, @RequestBody NotificationSettingsRequestDTO settingsRequest) {

        Map<String, Object> response = new HashMap<>();

        if (currentCompanyDetails == null) {
            response.put("success", false);
            response.put("message", "Authentication required. Unable to identify current company.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Company currentCompany = userService.getCurrentCompany();
        if (currentCompany == null) {

            response.put("success", false);
            response.put("message", "Failed to retrieve company details.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }


        try {
            CompanySetting companySetting = currentCompany.getCompanySetting();

            if (companySetting == null) {
                log.warn("CompanySetting not found for company ID: {}. Creating new one.", currentCompany.getId());
                companySetting = new CompanySetting();

                currentCompany.setCompanySetting(companySetting);
            }

            companySetting.setEmailOnNewApplicant(settingsRequest.isEmailNewApplicant());

            companyService.save(currentCompany);

            response.put("success", true);
            response.put("message", "Notification preference updated successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating notification settings for company ID {}: {}", currentCompany.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred while updating preferences.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/account/deletion/verify-password")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyPasswordForDeletion(@Valid @RequestBody VerifyPasswordRequest request) throws MessagingException {

        Company company = userService.getCurrentCompany();

        String companyEmail = companyService.initiateAccountDeletion(request.password());

        Map<String, String> responseData = Map.of("email", companyEmail);
        return ResponseEntity.ok(ApiResponse.success(responseData, "Password verified. A confirmation code has been sent to your email."));

    }

    @PostMapping("/account/deletion/finalize")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, String>>> finalizeAccountDeletion(@RequestBody Map<String, String> requestBody, HttpSession session) {
        String code = requestBody.get("verificationCode");
        try {
            companyService.finalizeAccountDeletion(code);

            // Invalidate session and clear security context
            SecurityContextHolder.clearContext();
            session.invalidate();

            Map<String, String> data = Map.of("redirectUrl", "/main/home");
            return ResponseEntity.ok(ApiResponse.success(data, "Your account has been permanently deleted."));

        } catch (InvalidPasswordChangeException e) { // Bắt lỗi xác thực mã
            log.warn("Finalize account deletion failed due to invalid code: {}", e.getErrors());
            return ResponseEntity.badRequest().body(ApiResponse.validationError("Deletion failed. Please check the code and try again.", e.getErrors()));
        } catch (Exception e) { // Bắt các lỗi không mong muốn khác
            log.error("Unexpected error during final account deletion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DELETION_ERROR", "An unexpected error occurred. Please contact support."));
        }
    }


    @PostMapping("/resend-verification-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resendCompanyVerificationCode(@RequestParam String email) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            log.info("🔄 Resending verification code to company email: {}", email);
//
//            if (email == null || email.trim().isEmpty()) {
//                response.put("success", false);
//                response.put("message", "Email is required");
//                return ResponseEntity.badRequest().body(response);
//            }
//
//            String verificationCode = emailService.generateVerificationCode();
//
//            emailService.sendVerificationCode(email, verificationCode);
//
//            emailService.storeVerificationCode(email, verificationCode);
//
//            log.info("✅ Verification code resent successfully to: {}", email);
//
//            response.put("success", true);
//            response.put("message", "Verification code has been resent to your email");
//            response.put("email", email);
//            response.put("remainingTime", 600);
//
//            return ResponseEntity.ok(response);
//
//        } catch (MessagingException e) {
//            log.error("❌ Failed to resend verification email to {}: {}", email, e.getMessage(), e);
//            response.put("success", false);
//            response.put("message", "Failed to send verification email. Please try again later.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//
//        } catch (Exception e) {
//            log.error("❌ Unexpected error during resend verification for {}: {}", email, e.getMessage(), e);
//            response.put("success", false);
//            response.put("message", "An unexpected error occurred. Please try again.");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
        return null;
    }

    @GetMapping("/transaction-history")
    public String getTransactionHistory(Model model, @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {

        GetTransactionHistoryResponse response = transactionService.getTransactionHistory(pageable);
        model.addAttribute("response", response);
        return "company/transaction-history";
    }

    @GetMapping("/api/transaction-history")
    @ResponseBody
    public GetTransactionHistoryResponse getTransactionHistoryApi(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        GetTransactionHistoryResponse response = transactionService.getTransactionHistory(pageable);
        return response;
    }

    /**
     * [NEW] Handles the request to export applicants for a specific job.
     * This endpoint is called by the "Export" button on the applicant viewing page.
     *
     * @param jobId  The ID of the job whose applicants are to be exported.
     * @param format The desired file format (e.g., "csv", "excel").
     * @return A ResponseEntity containing the file as a Resource.
     */
    @GetMapping("/export/applicants")
    @ResponseBody
    public ResponseEntity<Resource> exportApplicants(
            @RequestParam Long jobId,
            @RequestParam(defaultValue = "csv") String format) {

        ExportResult exportResult = exportService.exportApplicantsForJob(jobId, format);

        ByteArrayResource resource = new ByteArrayResource(exportResult.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(exportResult.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + exportResult.getFileName() + "\"")
                .body(resource);
    }
}
