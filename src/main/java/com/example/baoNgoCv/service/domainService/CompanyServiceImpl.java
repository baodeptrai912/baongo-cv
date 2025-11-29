package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.event.company.CreatedCompanyAccountEvent;
import com.example.baoNgoCv.event.company.SubscriptionDowngradedEvent;
import com.example.baoNgoCv.event.company.CompanyAccountDeletedEvent;
import com.example.baoNgoCv.event.company.UpgradePlanSuccessEvent;
import com.example.baoNgoCv.exception.companyException.InvalidPasswordChangeException;
import com.example.baoNgoCv.jpa.projection.company.CompanyProfileProjection;
import com.example.baoNgoCv.model.dto.company.*;

import com.example.baoNgoCv.model.dto.jobposting.context.JobCardDTO;
import com.example.baoNgoCv.model.enums.*;
import com.example.baoNgoCv.event.company.PutInformationRequestEvent;
import com.example.baoNgoCv.exception.companyException.CompanyNotFoundException;
import com.example.baoNgoCv.exception.utilityException.FileUploadException;
import com.example.baoNgoCv.exception.registrationException.DuplicateRegistrationDataException;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.jpa.repository.*;
import com.example.baoNgoCv.jpa.projection.company.CompanyDetailDTO;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final JobPostingServiceImpl jobPostingServiceImpl;
    private final UserServiceImpl userServiceImpl;
    private final ApplicationEventPublisher eventPublisher;
    private final FileService fileService;
    private final JobPostingRepository jobPostingRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;


    @Override
    public void upgradeCurrentPlan(Long companyId) {
        //1. Tìm kiếm công ty theo ID, nếu không tìm thấy sẽ ném ra ngoại lệ CompanyNotFoundException.
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException());

        //2. Nâng cấp gói tài khoản của công ty lên PREMIUM.
        company.upgradeSubscription(AccountTier.PREMIUM, null);

        //3. Tạo một sự kiện UpgradePlanSuccessEvent chứa thông tin chi tiết về công ty và gói tài khoản mới.
        UpgradePlanSuccessEvent event = new UpgradePlanSuccessEvent(
                company.getId(),
                company.getUsername(),
                company.getContactEmail(),
                company.getName(),
                company.getSubscriptionDetails().getAccountTier()
        );
        //4. Xuất bản sự kiện để các thành phần khác trong ứng dụng có thể lắng nghe và xử lý (ví dụ: gửi email thông báo).
        eventPublisher.publishEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCompaniesResponse getCompaniesData(int page, String sort,
                                                 String keyword,
                                                 List<IndustryType> industries, // <--- SỬA: Nhận List thay vì 1 giá trị
                                                 LocationType location) {

        User currentUser = userService.getCurrentUserOrNull();

        // GÁN MỘT LẦN DUY NHẤT
        final Set<Long> followedCompanyIds =
                (currentUser != null)
                        ? companyRepository.findFollowedCompanyIdsByUserId(currentUser.getId())
                        : Collections.emptySet();

        // sortObj giữ nguyên logic cũ
        Sort sortObj;
        if ("jobs".equalsIgnoreCase(sort)) {
            sortObj = Sort.by("companyMetric.openJobCount").descending();
        } else if ("interviews".equalsIgnoreCase(sort)) {
            sortObj = Sort.by("companyMetric.totalInterviewCount").descending();
        } else {
            sortObj = Sort.by("companyMetric.openJobCount").descending()
                    .and(Sort.by("companyMetric.totalInterviewCount").descending());
        }

        Pageable pageable = PageRequest.of(page, 9, sortObj);

        // GỌI REPOSITORY VỚI LIST (Xem phần Repository bên dưới để đảm bảo khớp)
        Page<Company> companyPage = companyRepository.searchCompanies(keyword, industries, location, pageable);

        // MAP DTO (Giữ nguyên)
        Page<GetCompaniesResponse.CompanyListDTO> dtoPage = companyPage.map(company -> {
            CompanyMetric metric = company.getCompanyMetric();
            int openJobs      = metric != null && metric.getOpenJobCount() != null ? metric.getOpenJobCount() : 0;
            int interviews    = metric != null && metric.getTotalInterviewCount() != null ? metric.getTotalInterviewCount() : 0;
            int followerCount = metric != null && metric.getFollowerCount() != null ? metric.getFollowerCount() : 0;

            return new GetCompaniesResponse.CompanyListDTO(
                    company.getId(),
                    company.getName(),
                    company.getCompanyLogo(),
                    company.getLocation(),
                    company.getIndustry() != null ? company.getIndustry().getDisplayName() : "N/A",
                    openJobs,
                    interviews,
                    formatFollowers(followerCount),
                    followedCompanyIds.contains(company.getId())
            );
        });

        List<String> availableIndustries = Arrays.stream(IndustryType.values())
                .map(Enum::name)
                .toList();

        List<String> availableLocations = Arrays.stream(LocationType.values())
                .map(Enum::name)
                .toList();

        return new GetCompaniesResponse(
                dtoPage,
                availableIndustries,
                availableLocations,
                "Find Your Next Great Workplace",
                sort,
                (int) companyPage.getTotalElements()
        );
    }





    /**
     * Hàm tiện ích để định dạng số lượng người theo dõi.
     * Ví dụ: 1234 -> "1.2k", 1234567 -> "1.2M"
     * @param count Số lượng người theo dõi.
     * @return Chuỗi đã được định dạng.
     */
    private String formatFollowers(int count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        if (count < 1_000_000) {
            // Dùng %.1f để luôn có 1 chữ số sau dấu phẩy, ví dụ 1.0k, 1.2k
            return String.format("%.1fk", count / 1000.0).replace(".0k", "k");
        }
        return String.format("%.1fM", count / 1_000_000.0).replace(".0M", "M");
    }

    @Override
    public void downgradeExpiredAccounts() {
        //1. Tìm tất cả các tài khoản có gói trả phí đã hết hạn.
        List<Company> expiredCompanies = companyRepository.findExpiredPaidSubscriptions(AccountTier.FREE, LocalDateTime.now());

        log.info("Found {} expired paid accounts to downgrade.", expiredCompanies.size());
        //2. Lặp qua danh sách và thực hiện hạ cấp cho từng công ty.
        for (Company company : expiredCompanies) {
            downgradeCompanyToFree(company);
        }
    }

    private void downgradeCompanyToFree(Company company) {
        //1. Lấy gói tài khoản hiện tại của công ty.
        AccountTier oldTier = company.getSubscriptionDetails().getAccountTier();
        //2. Nếu đã là gói FREE, không thực hiện gì thêm.
        if (oldTier == AccountTier.FREE) {
            return; // Already free, do nothing
        }
        //3. Cập nhật lại trạng thái của gói đăng ký (ví dụ: chuyển is_active = false).
        company.getSubscriptionDetails().validateAndRefreshState();

        //4. Lưu lại thay đổi vào cơ sở dữ liệu.
        companyRepository.save(company);
        log.info("Downgraded company {} (ID: {}) from {} to FREE.", company.getName(), company.getId(), oldTier);

        //5. Bắn ra sự kiện để thông báo cho các hệ thống khác (ví dụ: gửi email, notification).
        eventPublisher.publishEvent(new SubscriptionDowngradedEvent(this, company.getId(), company.getName(), company.getContactEmail(), oldTier));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getById(long id) {
        //1. Tìm kiếm công ty bằng ID và trả về kết quả.
        return companyRepository.findById(id);
    }

    @Override
    public void followCompany(Long companyId, Long userId) {
        //1. Tìm kiếm User và Company dựa trên ID được cung cấp.
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Company> companyOpt = companyRepository.findById(companyId);

        //2. Nếu cả hai đều tồn tại, thiết lập mối quan hệ theo dõi hai chiều.
        if (userOpt.isPresent() && companyOpt.isPresent()) {
            User user = userOpt.get();
            Company company = companyOpt.get();

            user.getFollowedCompanies().add(company);
            company.getFollowers().add(user);

            //3. Lưu lại thay đổi vào cơ sở dữ liệu.
            companyRepository.save(company);
            userRepository.save(user);
        } else {

            System.out.println("User or Company not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowedByUser(Long companyId, Long userId) {
        //1. Tìm kiếm User và Company dựa trên ID.
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        User user = userOpt.get();
        Company company = companyOpt.get();
        //2. Kiểm tra xem công ty có nằm trong danh sách đang theo dõi của người dùng không.
        return user.getFollowedCompanies().contains(company);

    }

    @Override
    public void unfollowCompany(Long companyId, Long userId) {
        //1. Tìm kiếm User và Company dựa trên ID.
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Company> companyOpt = companyRepository.findById(companyId);

        //2. Nếu cả hai đều tồn tại, loại bỏ mối quan hệ theo dõi hai chiều.
        if (userOpt.isPresent() && companyOpt.isPresent()) {
            User user = userOpt.get();
            Company company = companyOpt.get();
            user.getFollowedCompanies().remove(company);
            company.getFollowers().remove(user);
            //3. Lưu lại thay đổi.
            companyRepository.save(company);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> findByName(String companyName) {
        //1. Tìm kiếm công ty bằng tên và trả về kết quả.
        return companyRepository.findByName(companyName);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> findByUserName(String username) {
        //1. Tìm kiếm công ty bằng username và trả về kết quả.
        return companyRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> findByEmail(String companyEmail) {
        //1. Tìm kiếm công ty bằng email liên hệ và trả về kết quả.
        return companyRepository.findByContactEmail(companyEmail);
    }

    @Override
    @Transactional
    public boolean deleteById(long id) {
        log.info("🔥 CompanyService.deleteById() called with ID: {}", id);

        try {
            //1. Kiểm tra ID hợp lệ.
            if (id <= 0) {
                log.error("❌ Invalid company ID: {}", id);
                throw new IllegalArgumentException("Invalid company ID: " + id);
            }

            //2. Tìm công ty theo ID.
            log.info("🔍 Finding company by ID: {}", id);
            Company company = companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + id));

            log.info("✅ Company found: {} (ID: {})", company.getName(), id);

            List<JobPosting> companyJobPostings = company.getJobs();

            //3. Nếu công ty có tin tuyển dụng, tiến hành xóa chúng.
            if (companyJobPostings != null && !companyJobPostings.isEmpty()) {
                log.info("🔍 Deleting {} job postings for company {}",
                        companyJobPostings.size(), id);

                List<JobPosting> jobsToDelete = new ArrayList<>(companyJobPostings);

                for (int i = 0; i < jobsToDelete.size(); i++) {
                    JobPosting jobPosting = jobsToDelete.get(i);
                    try {
                        log.debug("🗑️ Deleting job posting {}/{}: {} (ID: {})",
                                i + 1, jobsToDelete.size(), jobPosting.getTitle(), jobPosting.getId());

//                        jobPostingServiceImpl.deleteJobPostingPermanently(jobPosting.getId());

                        log.debug("✅ Job posting {} deleted successfully", jobPosting.getId());
                    } catch (Exception e) {
                        log.error("💥 Failed to delete job posting {}: {}", jobPosting.getId(), e.getMessage(), e);
                        throw new RuntimeException("Failed to delete job posting: " + jobPosting.getId(), e);
                    }
                }
                log.info("✅ All job postings deleted successfully");
            } else {
                log.info("ℹ️ No job postings found for company {}", id);
            }

            //4. Xóa thực thể công ty khỏi cơ sở dữ liệu.
            log.info("🔍 Deleting company entity...");
            companyRepository.deleteById(id);

            log.info("✅ Company {} deleted successfully", id);

            return true;

        } catch (Exception e) {
            log.error("❌ Failed to delete company {}: {}", id, e.getMessage(), e);
            log.error("📋 Full stack trace:", e);

            throw new RuntimeException("Company deletion failed for ID: " + id, e);
        }

    }

    @Override
    public Company save(Company company) {
        //1. Lưu một thực thể công ty và trả về thực thể đã được lưu.
        return companyRepository.save(company);
    }

    @Override
    public PutInformationResponse updateCompanyProfile(Long companyId, PutInformationRequest request) {
        log.info("Processing company profile update - companyId: {}", companyId);

        //1. Tìm công ty hiện tại dựa trên ID.
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException());

        String logoUrl = null;
        //2. Nếu có file logo mới được gửi lên, tiến hành lưu file và xóa file cũ.
        if (request.hasLogo()) {

            String oldLogoFileName = company.getCompanyLogo();

            try {
                String fileName = fileService.storeFile(request.getLogoFile());
                logoUrl = fileService.getFileUrl(fileName);

                fileService.safeDeleteOldFile(oldLogoFileName, "company logo");

            } catch (FileUploadException e) {
                log.error("Failed to upload logo for company: {}, error: {}", companyId, e.getMessage());
                throw new FileUploadException("Currently you can't update your avatar yet, please try again later!");
            } catch (Exception e) {
                log.error("Failed to upload logo for company: {}, error: {}", companyId, e.getMessage());
                throw new FileUploadException("Currently you can't update your avatar yet, please try again later!");
            }
        }

        //3. Cập nhật các thông tin hồ sơ khác vào thực thể Company.
        company.updateProfileInformation(
                request.getIndustry(),
                request.getCompanySize(),
                request.getDescription(),
                logoUrl
        );

        //4. Bắn sự kiện để thông báo về việc cập nhật thông tin.
        eventPublisher.publishEvent(new PutInformationRequestEvent(companyId));

        log.info("Company profile updated successfully - companyId: {}", companyId);

        //5. Trả về DTO chứa thông tin đã được cập nhật.
        return PutInformationResponse.from(company);
    }

    @Override
    public PutContactResponse updateCompanyContact(Long companyId, PutContactRequest request) {
        log.info("🔄 Updating company contact - companyId: {}", companyId);

        //1. Tìm công ty hiện tại dựa trên ID.
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException());

        company.updateContactInformation(
                request.getCompanyName(),
                request.getLocation(),
                request.getWebsite(),
                request.getContactPhone()
        );

        //3. Lưu lại các thay đổi vào cơ sở dữ liệu.
        Company savedCompany = companyRepository.save(company);
        log.info("✅ Company contact updated successfully - companyId: {}", companyId);

        //4. Trả về DTO chứa thông tin đã được cập nhật.
        return PutContactResponse.from(savedCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public GetProfileUpdateResponse getProfileUpdateResponse(Long id) {
        //1. Tìm công ty theo ID.
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException());
        //2. Chuyển đổi thực thể Company sang DTO để trả về cho frontend.
        GetProfileUpdateResponse response = GetProfileUpdateResponse.fromEntity(company, fileService);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GetCompanyDetailResponse getCompanyDetailComplete(Long companyId) {
        //1. Lấy thông tin chi tiết cơ bản của công ty bằng projection.
        CompanyDetailDTO companyDTO = companyRepository.findCompanyDetailById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException());

        // 2. Lấy job cards với constructor projection
        List<JobCardDTO> jobCardDTOs = jobPostingRepository.findJobCardsByCompanyId(companyId);

        // 3. Tối ưu hóa việc tải các yêu cầu công việc (requirements) bằng cách batch loading.
        if (!jobCardDTOs.isEmpty()) {
            // Lấy tất cả job IDs
            List<Long> jobIds = jobCardDTOs.stream()
                    .map(JobCardDTO::getId)
                    .toList();

            // Gửi một query duy nhất để lấy tất cả requirements cho các job ID đã tìm thấy.
            List<JobPostingRepository.RequirementProjection> allReqs = jobPostingRepository
                    .findTop3RequirementsByJobPostingIds(jobIds);

            // Group requirements by job ID
            Map<Long, List<String>> reqsByJobId = allReqs.stream()
                    .collect(Collectors.groupingBy(
                            JobPostingRepository.RequirementProjection::getJobPostingId,
                            Collectors.mapping(
                                    JobPostingRepository.RequirementProjection::getRequirement,
                                    Collectors.toList()
                            )
                    ));

            // Gán danh sách requirements vào từng DTO tương ứng.
            jobCardDTOs.forEach(dto -> {
                List<String> reqs = reqsByJobId.getOrDefault(dto.getId(), List.of())
                        .stream()
                        .limit(3) // Top 3 requirements
                        .toList();
                dto.setTopRequirements(reqs);
            });
        }
        if (jobCardDTOs.isEmpty()) {
            log.warn("⚠️  NO JOB CARDS FOUND for company ID: {}", companyId);

            // Debug: Kiểm tra có job posting nào trong DB không
            long totalJobs = jobPostingRepository.countByCompanyId(companyId);
            log.info("🔍 Total job postings in DB for company {}: {}", companyId, totalJobs);

            long activeJobs = jobPostingRepository.countByCompanyIdAndStatus(companyId, JobPostingStatus.OPEN);
            log.info("🔍 Active job postings for company {}: {}", companyId, activeJobs);
        } else {
            log.info("✅ Logging {} job cards:", jobCardDTOs.size());

            for (int i = 0; i < jobCardDTOs.size(); i++) {
                JobCardDTO job = jobCardDTOs.get(i);
                log.info("📄 Job #{}: ID={}, Title='{}', Location={}, Type={}, Status={}, Company={}",
                        i + 1,
                        job.getId(),
                        job.getTitle(),
                        job.getLocation() != null ? job.getLocation().getDisplayName() : "NULL",
                        job.getJobType() != null ? job.getJobType().getDisplayName() : "NULL",
                        "ACTIVE",
                        job.getCompanyName()
                );
            }
        }
        // 4. Lấy thông tin người dùng hiện tại để xác định trạng thái "followed".
        User currentUser = userService.getCurrentUserOrNull();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        Set<Long> companyFollowers = companyRepository.findFollowerIdsByCompanyId(companyId);

        // 5. Tạo đối tượng response cuối cùng, DTO sẽ tự tính toán logic "followed".
        return GetCompanyDetailResponse.create(companyDTO, jobCardDTOs, currentUserId, companyFollowers);
    }

    @Override
    @Transactional(readOnly = true)
    public GetCompanyProfileResponse getCompanyProfile(Long id) {
        //1. Sử dụng projection để lấy các thông tin cần thiết cho trang hồ sơ công ty.
        CompanyProfileProjection projection = companyRepository.findCompanyProfileProjectionById(id)
                .orElseThrow(() -> new CompanyNotFoundException());
        //2. Tạo và trả về DTO response.
        return new GetCompanyProfileResponse(projection);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrderAlreadyProcessed(Long orderCode) {
        log.info("Checking if order {} is already processed", orderCode);
        //1. Tìm công ty theo ID (ở đây ID công ty chính là orderCode).
        Company company = companyRepository.findById(orderCode)
                .orElseThrow(() -> new CompanyNotFoundException());

        //2. Kiểm tra xem gói tài khoản của công ty đã là PREMIUM hay chưa.
        boolean isProcessed = company.getSubscriptionDetails().getAccountTier() == AccountTier.PREMIUM;
        if (isProcessed) {
            log.warn("Order {} corresponds to a company that is already on the PREMIUM plan.", orderCode);
        }
        return isProcessed;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateOrderAmount(Long orderCode, Integer amount) {
        //1. Lấy giá tiền dự kiến của gói PREMIUM.
        Integer expectedAmount = AccountTier.PREMIUM.getPrice();
        //2. So sánh số tiền nhận được với số tiền dự kiến.
        boolean isValid = expectedAmount.equals(amount);
        if (!isValid) {
            log.error("Amount validation failed for orderCode: {}. Expected: {}, Received: {}", orderCode, expectedAmount, amount);
        }
        return isValid;
    }

    @Override
    public PasswordChangeInitResponse initiatePasswordChange(String currentPassword, String newPassword) throws MessagingException {
        //1. Lấy thông tin công ty đang đăng nhập.
        Company currentCompany = userService.getCurrentCompany();
        log.debug("[PASSWORD_CHANGE_INIT] Starting initiation | CompanyID: {} | Email: {}", currentCompany.getId(), currentCompany.getContactEmail());


        //2. Xác thực mật khẩu hiện tại của công ty.
        log.debug("[PASSWORD_CHANGE_INIT] Validating current password for CompanyID: {}", currentCompany.getId());
        validateCurrentPassword(currentPassword, currentCompany);
        log.debug("[PASSWORD_CHANGE_INIT] Validation successful for CompanyID: {}", currentCompany.getId());

        //3. Nếu mật khẩu đúng, lấy email của công ty.
        String companyEmail = currentCompany.getContactEmail();

        try {
            //4. Gửi mã xác thực đến email của công ty.
            log.debug("[PASSWORD_CHANGE_INIT] Attempting to send verification email to: {}", companyEmail);
            emailService.sendVerification(companyEmail, VerificationType.PASSWORD_CHANGE);
            log.debug("[PASSWORD_CHANGE_INIT] Email sent successfully to: {}", companyEmail);
        } catch (MessagingException e) {
            log.error("[PASSWORD_CHANGE_INIT] Failed to send email to: {} | Error: {}", companyEmail, e.getMessage());
            throw e;
        }

        //5. Trả về DTO chứa thông tin phản hồi cho frontend.
        log.info("[PASSWORD_CHANGE_INIT] Completed successfully for CompanyID: {}", currentCompany.getId()); // Dùng INFO cho kết thúc thành công quan trọng
        return new PasswordChangeInitResponse(
                true,
                "Verification code sent to your company email: " + companyEmail,
                companyEmail
        );
    }

    @Override
    public void finalizePasswordChange(String code, String pendingEmail, String newPassword) {
        //1. Lấy thông tin công ty đang đăng nhập.
        Company currentCompany = userService.getCurrentCompany();

        //2. Xác thực session: kiểm tra email trong session có khớp với email của người dùng hiện tại không.
        if (currentCompany == null || pendingEmail == null || !pendingEmail.equals(currentCompany.getContactEmail())) {
            throw new IllegalStateException("Verification session expired or invalid. Please start over.");
        }

        //3. Xác thực mật khẩu mới lấy từ session.
        if (newPassword == null || newPassword.length() < 6) { // Match DTO validation
            throw new IllegalStateException("New password not found in session or is invalid.");
        }

        //4. Xác thực mã OTP người dùng nhập vào.
        emailService.verifyCode(pendingEmail, code);

        //5. Nếu mọi thứ hợp lệ, mã hóa và cập nhật mật khẩu mới vào cơ sở dữ liệu.
        currentCompany.setPassword(passwordEncoder.encode(newPassword));
        companyRepository.save(currentCompany);


    }

    private void validateCurrentPassword(String currentPassword, Company company) throws InvalidPasswordChangeException {
        Map<String, String> errors = new HashMap<>();

        //1. So sánh mật khẩu người dùng nhập với mật khẩu đã được mã hóa trong DB.
        if (!passwordEncoder.matches(currentPassword, company.getPassword())) {
            errors.put("currentPassword", "Incorrect current password.");
        }

        if (!errors.isEmpty()) {
            throw new InvalidPasswordChangeException(errors);
        }
    }

    @Override
    public void registerNewCompany(PostRegisterRequest request) {
        //1. Lấy quyền (role) mặc định cho tài khoản công ty từ cơ sở dữ liệu.
        Permission companyRole = permissionRepository.findByName("ROLE_COMPANY")
                .orElseThrow(() -> new IllegalStateException("Default 'COMPANY' role not found in database. Please ensure it exists."));

        //2. Sử dụng factory method trong entity Company để tạo một đối tượng mới.
        Company newCompany = Company.createFromRegistrationRequest(request, passwordEncoder, companyRole);

        //3. Lưu thực thể công ty mới vào cơ sở dữ liệu.
        Company savedCompany = companyRepository.save(newCompany);

        //4. Bắn sự kiện để thông báo cho các hệ thống khác (gửi email, notification, etc.).
        CreatedCompanyAccountEvent event = new CreatedCompanyAccountEvent(
                savedCompany.getId(),
                savedCompany.getName(),
                savedCompany.getContactEmail()
        );
        eventPublisher.publishEvent(event);
        log.info("Published CreatedCompanyAccountEvent for new company: {}", savedCompany.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateDuplicateInfo(PostRegisterRequest request) {
        //1. Kiểm tra xem username hoặc email đã tồn tại trong hệ thống (cả User và Company) hay chưa.
        if (userRepository.existsByUsernameInUsersOrCompanies(request.username()) > 0) {
            throw new DuplicateRegistrationDataException("username", "This username is already taken.");
        }
        if (userRepository.existsByEmailInUsersOrCompanies(request.contactEmail()) > 0) {
            throw new DuplicateRegistrationDataException("contactEmail", "This email is already registered.");
        }
    }

    @Override
    public String initiateAccountDeletion(String password) throws MessagingException {
        //1. Lấy thông tin công ty đang đăng nhập.
        Company currentCompany = userService.getCurrentCompany();

        //2. Xác thực mật khẩu người dùng cung cấp.
        if (!passwordEncoder.matches(password, currentCompany.getPassword())) {
            throw new InvalidPasswordChangeException(Map.of("password", "Incorrect password."));
        }

        //3. Nếu mật khẩu đúng, gửi mã xác thực đến email của công ty.
        String email = currentCompany.getContactEmail();
        try {
            emailService.sendVerification(email, VerificationType.ACCOUNT_DELETION);
            log.info("Account deletion verification code sent to company: {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send deletion verification email to {}: {}", email, e.getMessage());
            throw e;
        }
        //4. Trả về email đã gửi để hiển thị cho người dùng.
        return email;
    }

    @Override
    @Transactional
    public void finalizeAccountDeletion(String verificationCode) {
        //1. Lấy thông tin công ty đang đăng nhập.
        Company currentCompany = userService.getCurrentCompany();
        //2. Xác thực mã OTP người dùng nhập vào.
        try {
            emailService.verifyCode(currentCompany.getContactEmail(), verificationCode);
        } catch (com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException e) {
            throw new InvalidPasswordChangeException(Map.of("verificationCode", "Invalid or expired verification code."));
        }

        //3. Thu thập các thông tin cần thiết (ID, tên, email, logo, followers, applicants) TRƯỚC KHI xóa.
        Long companyId = currentCompany.getId();
        String companyName = currentCompany.getName();
        String companyEmail = currentCompany.getContactEmail();
        String logoPath = currentCompany.getCompanyLogo();

        Set<Long> followerUserIds = currentCompany.getFollowers().stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<Long> applicantUserIds = currentCompany.getJobs().stream()
                .flatMap(job -> job.getApplicants().stream())
                .map(applicant -> applicant.getUser().getId())
                .collect(Collectors.toSet());

        //4. Xóa thực thể công ty khỏi cơ sở dữ liệu.
        companyRepository.delete(currentCompany);
        log.info("Successfully deleted company account for ID: {}, Name: {}", companyId, companyName);

        //5. Bắn sự kiện chứa các thông tin đã thu thập để các hệ thống khác xử lý (gửi email, xóa file, etc.).
        eventPublisher.publishEvent(new CompanyAccountDeletedEvent(companyId, companyName, companyEmail, logoPath, followerUserIds, applicantUserIds));
        log.info("Published CompanyAccountDeletedEvent for company: {}", companyName);
    }
}
