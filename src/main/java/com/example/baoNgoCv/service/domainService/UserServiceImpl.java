package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.event.user.UserAccountDeletedEvent;
import com.example.baoNgoCv.exception.emailException.EmailSendingException;
import com.example.baoNgoCv.exception.jobseekerException.*;
import com.example.baoNgoCv.exception.securityException.InvalidPasswordException;
import com.example.baoNgoCv.model.dto.common.VerifyPasswordRequest;
import com.example.baoNgoCv.model.dto.user.PostSocialLinkRequest;
import com.example.baoNgoCv.model.dto.NotificationSettingsDto;
import com.example.baoNgoCv.model.dto.PersonalInforUpdateDTO;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse;
import com.example.baoNgoCv.model.dto.user.*;
import com.example.baoNgoCv.model.enums.VerificationType;
import com.example.baoNgoCv.model.valueObject.SocialLink;
import com.example.baoNgoCv.model.enums.ExpireReason;
import com.example.baoNgoCv.event.user.UserRegisteredEvent;
import com.example.baoNgoCv.event.applicant.ApplicationSubmittedEvent;
import com.example.baoNgoCv.event.jobposting.JobPostingExpiredEvent;
import com.example.baoNgoCv.exception.educationException.EducationNotFoundException;
import com.example.baoNgoCv.exception.utilityException.FileUploadException;
import com.example.baoNgoCv.exception.jobpostingException.JobNotFoundExceptionJson;
import com.example.baoNgoCv.model.entity.*;
import com.example.baoNgoCv.model.valueObject.ContactInfo;
import com.example.baoNgoCv.model.enums.Skill;
import com.example.baoNgoCv.model.valueObject.PersonalInfo;
import com.example.baoNgoCv.jpa.repository.*;

import com.example.baoNgoCv.jpa.projection.user.BasicPersonalInfoDTO;
import com.example.baoNgoCv.jpa.projection.user.BasicProfileResponse;
import com.example.baoNgoCv.jpa.projection.user.EducationDTO;
import com.example.baoNgoCv.model.enums.SocialPlatform;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import com.example.baoNgoCv.model.session.PendingUserRegistration;
import com.example.baoNgoCv.service.validationService.JobApplicationValidationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final SessionRegistry sessionRegistry;
    private final ApplicantRepository applicantRepository;
    private final JobExperienceRepository jobExperienceRepository;
    private final EducationRepository educationRepository;
    private final FileService fileService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final JobPostingRepository jobPostingRepository;
    private final PermissionRepository permissionRepository;
    private final EmailService emailService;
    private final JobApplicationValidationService jobApplicationValidationService;

    @Value("${app.password-reset.session-duration-minutes:5}")
    private int passwordResetSessionDurationMinutes;

    @Override
    public User findByUsername(String username) {
        // 1. Try to find a 'User' account first.
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get();
        }
   return null;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileView getProfileForViewing(Long userId) {
        // 1. Lấy user gốc từ DB (bao gồm cả các collection liên quan)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        // 2. Sử dụng factory method trong DTO để tạo view model
        return UserProfileView.fromUser(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByContactInfo_Email(email);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new IllegalStateException("No authenticated user found in SecurityContext");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException());

        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    @Override
    public User getCurrentUserOrNull() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // No authenticated user, return null safely.
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @Override
    public Company getCurrentCompany() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<Company> companyOptional = companyRepository.findByUsername(userDetails.getUsername());

            if (companyOptional.isPresent()) {
                return companyOptional.get();
            }
        }
        return null;
    }


    @Override
    public Boolean checkPassword(User user, String currentPassword) {

        return passwordEncoder.matches(currentPassword, user.getPassword());


    }

    @Override
    public Boolean isPhoneNumber(long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isPresent()) {

            User foundUser = user.get();


            if (foundUser.getContactInfo().getPhoneNumber() != null && !foundUser.getContactInfo().getPhoneNumber().isEmpty()) {
                return true;
            }
        }


        return false;
    }

    @Override
    public boolean isUserProfileComplete(User user) {
        // Kiểm tra các trường thông tin bắt buộc
        return user.getPersonalInfo().getFullName() != null && !user.getPersonalInfo().getFullName().isEmpty() &&
                user.getContactInfo().getEmail() != null && !user.getContactInfo().getEmail().isEmpty() &&
                user.getContactInfo().getPhoneNumber() != null && !user.getContactInfo().getPhoneNumber().isEmpty() &&
                user.getPersonalInfo().getDateOfBirth() != null &&
                user.getPersonalInfo().getNationality() != null && !user.getPersonalInfo().getNationality().isEmpty() &&
                user.getPersonalInfo().getGender() != null && !user.getPersonalInfo().getGender().isEmpty();
    }

    @Override
    @Transactional
    public void addFollower(Long companyId) {
        // 1. Load Company entity trong transaction hiện tại
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // 2. Lấy userId từ SecurityContext (chỉ lấy ID, không dùng entity từ Authentication)
        User currentUserFromAuth = getCurrentUser();
        Long userId = currentUserFromAuth.getId();

        // 3. Load lại User entity TRONG transaction này (để Hibernate track được)
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Kiểm tra đã follow chưa
        if (company.getFollowers().contains(currentUser)) {
            throw new RuntimeException("User is already following this company");
        }

        // 5. Thêm user vào danh sách followers
        company.getFollowers().add(currentUser);

        // 6. [FIX NullPointerException] Kiểm tra CompanyMetric trước khi tăng follower
        CompanyMetric metric = company.getCompanyMetric();
        if (metric != null) {
            metric.incFollower();
        } else {
            // [FIX cho Shared PK] Không set ID thủ công, @MapsId sẽ tự động lấy
            metric = new CompanyMetric();
            metric.setFollowerCount(1);
            metric.setOpenJobCount(0);
            metric.setTotalInterviewCount(0);

            // Thiết lập quan hệ 2 chiều (quan trọng với @MapsId)
            metric.setCompany(company);
            company.setCompanyMetric(metric);
        }

        companyRepository.save(company);
    }


    @Override
    @Transactional
    public void removeFollower(Long companyId) {
        // 1. Load Company entity trong transaction hiện tại
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // 2. Lấy userId từ SecurityContext (chỉ lấy ID, không dùng entity từ Authentication)
        User currentUserFromAuth = getCurrentUser();
        Long userId = currentUserFromAuth.getId();

        // 3. Load lại User entity TRONG transaction này
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 4. Kiểm tra có đang follow không
        if (!company.getFollowers().contains(currentUser)) {
            throw new RuntimeException("User is not following this company");
        }

        // 5. Xóa user khỏi danh sách followers
        company.getFollowers().remove(currentUser);

        // 6. [FIX NullPointerException] Kiểm tra CompanyMetric trước khi giảm follower
        CompanyMetric metric = company.getCompanyMetric();
        if (metric != null) {
            metric.decFollower();
        } else {
            // Log warning nếu metric bị thiếu
            // (không nên tạo mới vì đang unfollow, không hợp lý có metric = 0)
            throw new RuntimeException("CompanyMetric not found for company: " + companyId);
        }

        // 7. Save company (Hibernate sẽ dirty check và update cả metric)
        companyRepository.save(company);
    }


    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        user.getFollowedCompanies().clear();

        userRepository.flush();

        userRepository.delete(user);

    }

    /**
     * ✅ REFACTORED: Cập nhật cài đặt thông báo.
     * Phương thức này giờ nhận một giá trị boolean, không phụ thuộc vào DTO.
     */
    @Override
    @Transactional
    public void updateNotificationSettings(String username, boolean emailOnUpdate) {
        log.info("Updating notification settings for user: {} to {}", username, emailOnUpdate);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        UserSettings settings = user.getUserSettings();
        if (settings == null) {
            // This case should ideally not happen due to the logic in User.createNew()
            throw new IllegalStateException("Data inconsistency: UserSettings is null for user " + username);
        }

        settings.setEmailOnApplicationUpdate(emailOnUpdate);
        // No need to save, @Transactional handles it.
    }

    /**
     * ✅ REFACTORED: Cập nhật cài đặt quyền riêng tư.
     * Phương thức này giờ nhận một giá trị boolean, không phụ thuộc vào DTO.
     */
    @Override
    @Transactional
    public void updatePrivacySettings(String username, boolean isPublic) {
        log.info("Updating privacy settings for user: {} to {}", username, isPublic ? "PUBLIC" : "PRIVATE");

        // 1. Tìm người dùng, nếu không thấy sẽ ném UsernameNotFoundException
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 2. Lấy đối tượng UserSettings (đã được đảm bảo không null khi tạo User)
        UserSettings settings = user.getUserSettings();
        if (settings == null) {
            throw new IllegalStateException("Data inconsistency: UserSettings is null for user " + username);
        }

        // 3. Cập nhật trạng thái và lưu lại (do có @Transactional, JPA sẽ tự động lưu)
        settings.setProfilePublic(isPublic);

    }

    @Override
    public String findEmailByUsername(String username) {
        // ✅ IMPROVED: Centralized logic to find email from either User or Company.

        // 1. Try to find a 'User' account first.
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check if contact info and email are not null
            if (user.getContactInfo() != null && user.getContactInfo().getEmail() != null) {
                return user.getContactInfo().getEmail();
            }
        }

        // 2. If not found as a User (or user has no email), try to find a 'Company' account.
        Optional<Company> companyOptional = companyRepository.findByUsername(username);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            // Check if contact email is not null and not empty
            return company.getContactEmail();
        }

        // 3. If neither is found or no email is configured, return null.
        return null;
    }


    private void invalidateAllUserSessions(String username) {
        log.info("Starting session invalidation for user: {}", username);

        try {
            List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
            log.debug("Total principals in SessionRegistry: {}", allPrincipals.size());

            for (Object principal : allPrincipals) {
                String principalUsername = null;
                if (principal instanceof UserDetails) {
                    principalUsername = ((UserDetails) principal).getUsername();
                }

                if (username.equals(principalUsername)) {
                    log.info("Found matching principal for session invalidation: {}", username);
                    List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);

                    if (sessions != null && !sessions.isEmpty()) {
                        log.info("Invalidating {} active session(s) for user: {}", sessions.size(), username);
                        for (SessionInformation sessionInfo : sessions) {
                            sessionInfo.expireNow();
                            log.debug("Expired session ID: {}", sessionInfo.getSessionId());
                        }
                    } else {
                        log.info("No active sessions found for user: {}", username);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error during session invalidation for user {}: ", username, e);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("🔍 ============================================");
        System.out.println("🔍 [loadUserByUsername] Searching for: " + username);

        // 1. Try to find User first
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("✅ [loadUserByUsername] User found!");
            System.out.println("   👤 Username: " + user.getUsername());
            System.out.println("   📦 Permissions loaded: " + user.getPermissions().size());

            // Log each permission
            user.getPermissions().forEach(p ->
                    System.out.println("      🔑 Permission: " + p.getName())
            );

            System.out.println("🔍 ============================================");
            return user;
        }

        // 2. If User not found, try Company
        Optional<Company> companyOptional = companyRepository.findByUsername(username);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            System.out.println("✅ [loadUserByUsername] Company found!");
            System.out.println("   🏢 Username: " + company.getUsername());
            System.out.println("   📦 Permissions loaded: " + company.getPermissions().size());

            // Log each permission
            company.getPermissions().forEach(p ->
                    System.out.println("      🔑 Permission: " + p.getName())
            );

            System.out.println("🔍 ============================================");
            return company;
        }

        // 3. If neither found, throw exception
        System.out.println("❌ [loadUserByUsername] NOT FOUND: " + username);
        System.out.println("🔍 ============================================");
        throw new UsernameNotFoundException("Invalid username or password.");
    }


    private Set<? extends GrantedAuthority> getPermission(Set<Permission> permissions) {
        return permissions.stream().map(permission -> new SimpleGrantedAuthority(permission.getName())).collect(Collectors.toSet());

    }

    @Override
    @Transactional
    public void processPasswordChangeAndInvalidateSessions(String username, String newPassword) {
        // ✅ IMPROVED: Handle password change for both User and Company accounts.
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 1. Try to find and update a 'User' account.
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(encodedPassword);
            userRepository.save(user);
            log.info("Password changed for user: {}", username);
        } else {
            // 2. If not a User, try to find and update a 'Company' account.
            Optional<Company> companyOptional = companyRepository.findByUsername(username);
            if (companyOptional.isPresent()) {
                Company company = companyOptional.get();
                company.setPassword(encodedPassword);
                companyRepository.save(company);
                log.info("Password changed for company: {}", username);
            } else {
                // 3. If neither is found, throw an exception.
                throw new UsernameNotFoundException("Account not found for username: " + username);
            }
        }

        //3 Invalidate all sessions for this user
        invalidateAllUserSessions(username);

        //4 Log the operation
        log.info("Password changed and sessions invalidated for user: {}", username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Hoàn tất quá trình đăng ký người dùng sau khi xác minh email thành công.
     * Phương thức này tạo và lưu một thực thể Người dùng mới vào cơ sở dữ liệu.
     *
     * @param pendingRegistration Dữ liệu đăng ký đang chờ xử lý chứa thông tin người dùng.
     * @throws IllegalStateException nếu không tìm thấy quyền 'USER' mặc định.
     */
    @Override
    @Transactional
    public void completeRegistration(PendingUserRegistration pendingRegistration) {
        log.debug("[REGISTRATION] Starting completeRegistration for username: {}", pendingRegistration.getUsername());

        // 1. Lấy quyền 'USER' mặc định từ cơ sở dữ liệu
        log.debug("[REGISTRATION] Attempting to find default 'ROLE_USER' permission.");
        Permission userPermission = permissionRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("Default 'USER' permission not found. Please ensure it exists in the database."));
        log.debug("[REGISTRATION] Found 'ROLE_USER' permission with ID: {}", userPermission.getId());

        // 2. Sử dụng static factory method trong User để tạo một thực thể mới
        log.debug("[REGISTRATION] Creating new User entity from pending registration data.");
        User newUser = User.createNew(
                pendingRegistration.getUsername(),
                pendingRegistration.getPassword(),
                pendingRegistration.getEmail(),
                userPermission,
                passwordEncoder
        );
        log.debug("[REGISTRATION] New User entity created for username: {}", newUser.getUsername());

        // 3. Lưu người dùng mới vào cơ sở dữ liệu
        log.debug("[REGISTRATION] Saving new user to database.");
        User savedUser = userRepository.save(newUser);

        // 4. Bắn sự kiện đăng ký thành công để các listener khác xử lý (ví dụ: gửi thông báo)
        // Sự kiện này sẽ được xử lý sau khi transaction này commit thành công.
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser.getId(), savedUser.getUsername(), savedUser.getContactInfo().getEmail()));

        log.info("User registration completed for username: {}. Published UserRegisteredEvent.", savedUser.getUsername());
    }

    @Override
    @Transactional
    public void updateSocialLinks(List<PostSocialLinkRequest> socialLinksRequest) {
        User currentUser = getCurrentUser();

        // Xử lý trường hợp xóa tất cả
        if (socialLinksRequest == null || socialLinksRequest.isEmpty()) {
            currentUser.getSocialLinks().clear();
            return;
        }

        // Validation 2: Kiểm tra platform trùng lặp
        Set<SocialPlatform> seenPlatforms = new HashSet<>();
        for (PostSocialLinkRequest request : socialLinksRequest) {
            if (request.platform() != null && !seenPlatforms.add(request.platform())) {
                throw new InvalidSocialLinksException(
                        "Duplicate platform detected: " + request.platform()
                );
            }
        }

        // Chuyển đổi sang entity và cập nhật
        Set<SocialLink> newSocialLinks = socialLinksRequest.stream()
                .map(request -> new SocialLink(request.platform(), request.url()))
                .collect(Collectors.toSet());

        currentUser.updateAllSocialLinks(newSocialLinks);
    }

    /**
     * Cập nhật hoặc thay thế toàn bộ danh sách kỹ năng cho người dùng hiện tại.
     * <p>
     * Nếu danh sách đầu vào là {@code null} hoặc rỗng, tất cả các kỹ năng hiện tại sẽ bị xóa.
     * </p>
     *
     * @param skillsRequest Danh sách các enum {@link Skill} đại diện cho trạng thái mới.
     */
    @Override
    @Transactional
    public PostUpdateSkillResponse updateSkills(List<Skill> skillsRequest) {
        User currentUser = getCurrentUser();
        log.info("Updating skills for user ID: {}", currentUser.getId());
        List<String> skillDisplayNames = skillsRequest.stream()
                .map(Skill::getDisplayName)
                .toList();
        List<Skill> skills = (skillsRequest != null) ? skillsRequest : Collections.emptyList();
        Set<Skill> newSkills = new HashSet<>(skills);

        currentUser.updateAllSkills(newSkills);
        log.info("Successfully updated {} skills for user ID: {}", newSkills.size(), currentUser.getId());

        return new PostUpdateSkillResponse(
                skillDisplayNames,
                "Skills updated successfully",
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public PostVerifyPasswordForDeletionResponse verifyPasswordToDeleteAccount(VerifyPasswordRequest request) {

        // 1. Lấy người dùng đang đăng nhập
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated to perform this action.");
        }

        // 2. Xác thực mật khẩu
        if (!checkPassword(currentUser, request.password())) {
            throw new InvalidPasswordException("Incorrect password provided.");
        }

        // 3. Tạo và gửi mã xác thực
        String userEmail = currentUser.getContactInfo().getEmail();

        try {
            emailService.sendVerification(userEmail, VerificationType.ACCOUNT_DELETION);
        } catch (MessagingException e) {
            // Gói lại lỗi gửi mail trong một exception nghiệp vụ
            throw new EmailSendingException("Failed to send account deletion email.", e);
        }

        // 4. Lấy thời gian hết hạn từ EmailService để trả về
        long expirationTimestamp = System.currentTimeMillis() + (emailService.getVerificationCodeExpirySeconds() * 1000);

        // 5. Trả về DTO response
        return new PostVerifyPasswordForDeletionResponse(userEmail, expirationTimestamp);
    }


    @Override
    @Transactional(readOnly = true)
    public GetProfileResponse getProfileData() {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        log.info("Fetching profile data for user ID: {}", userId);

        try {

            BasicProfileResponse basicProfile = userRepository.findBasicProfileById(userId)
                    .orElseThrow(() -> {
                        log.error("User not found with ID: {}", userId);
                        return new UserNotFoundException();
                    });

            List<JobExperienceDTO> jobExperiences =
                    jobExperienceRepository.findJobExperiencesByUserId(userId);
            log.debug("Found {} job experiences for user ID: {}", jobExperiences.size(), userId);

            List<EducationDTO> educations =
                    educationRepository.findEducationsByUserId(userId);
            log.debug("Found {} educations for user ID: {}", educations.size(), userId);

            // Lấy dữ liệu skills và socialLinks bằng các projection riêng biệt
            List<Skill> skillsList = userRepository.findSkillsByUserId(userId);
            List<SocialLink> socialLinksList = userRepository.findSocialLinksByUserId(userId);
            log.debug("Found {} skills for user ID: {}", skillsList.size(), userId);
            log.debug("Found {} social links for user ID: {}", socialLinksList.size(), userId);

            // Chuyển đổi sang Set để phù hợp với DTO và loại bỏ các phần tử trùng lặp (nếu có)
            Set<Skill> skills = new HashSet<>(skillsList);
            Set<SocialLink> socialLinks = new HashSet<>(socialLinksList);

            GetProfileResponse profileData = new GetProfileResponse(
                    basicProfile,
                    jobExperiences,
                    educations,
                    skills,
                    socialLinks,
                    userId
            );

            log.info("Successfully assembled profile data for user ID: {}", userId);
            return profileData;

        } catch (UserNotFoundException e) {
            log.error("User not found exception for user ID: {}", userId, e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching profile data for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch profile data", e);
        }
    }

    @Override
    public GetProfileUpdateResponse getProfileUpdateData() {

        User currentUser = getCurrentUser();

        Long userId = currentUser.getId();

        BasicProfileResponse basicProfile = userRepository.findBasicProfileById(userId)
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        List<EducationDTO> educations = educationRepository.findEducationsByUserId(userId);

        List<JobExperienceDTO> jobExperiences = jobExperienceRepository.findJobExperiencesByUserId(userId);

        return GetProfileUpdateResponse.from(basicProfile, educations, jobExperiences);
    }

    @Override
    public PostPersonalInfoResponse updatePersonalInfo(PersonalInforUpdateDTO dto) {
        User user = getCurrentUser();
        if (user == null) {
            log.warn("Attempted to update personal info for unauthenticated user.");
            throw new UserNotFoundException();
        }
        log.info("Updating personal info for user ID: {}", user.getId());

        if (dto.getDateOfBirth() == null) {
            dto.setDateOfBirth(user.getPersonalInfo().getDateOfBirth());
        }

        boolean updated = false;
        String avatarUrl = user.getProfilePicture();

        PersonalInfo personal = user.getPersonalInfo();
        ContactInfo contact = user.getContactInfo();

        updated |= updateField(personal::setFullName, personal.getFullName(), dto.getFullName());
        updated |= updateField(contact::setPhoneNumber, contact.getPhoneNumber(), dto.getPhone());
        updated |= updateField(contact::setAddress, contact.getAddress(), dto.getLocation());
        updated |= updateField(personal::setGender, personal.getGender(), dto.getGender());
        updated |= updateField(personal::setDateOfBirth, personal.getDateOfBirth(), dto.getDateOfBirth());
        updated |= updateField(personal::setNationality, personal.getNationality(), dto.getNationality());

        MultipartFile avatarFile = dto.getAvatar();
        log.debug("Avatar file received: {}", (avatarFile != null ? avatarFile.getOriginalFilename() : "null"));
        log.debug("Avatar file empty status: {}", (avatarFile != null ? avatarFile.isEmpty() : "N/A"));

        if (avatarFile != null && !avatarFile.isEmpty()) { // Check if new avatar file is provided and not empty
            try {
                String oldAvatar = user.getProfilePicture();
                log.debug("Old avatar path for user {}: {}", user.getId(), oldAvatar);

                avatarUrl = fileService.getFileUrl(fileService.storeFile(avatarFile));
                log.debug("New avatar file stored and URL generated: {}", avatarUrl);

                if (avatarUrl == null || avatarUrl.isEmpty()) {
                    log.error("fileService.getFileUrl returned null or empty URL after storing avatar for user {}", user.getId());
                    throw new FileUploadException("Failed to generate avatar URL.");
                }

                user.setProfilePicture(avatarUrl);
                log.info("User {} profile picture updated to: {}", user.getId(), avatarUrl);

                if (oldAvatar != null && !oldAvatar.contains("default-avatar")) {
                    log.debug("Attempting to delete old avatar: {}", oldAvatar);
                    fileService.deleteFile(oldAvatar);
                    log.debug("Old avatar {} deleted successfully.", oldAvatar);
                }
                updated = true;
            } catch (FileUploadException e) {
                log.error("FileUploadException during avatar update for user {}: {}", user.getId(), e.getMessage());
                throw e; // Re-throw the specific exception
            } catch (Exception e) {
                log.error("Unexpected error during avatar update for user {}: {}", user.getId(), e.getMessage(), e);
                throw new FileUploadException("Avatar upload failed due to an unexpected error.");
            }
        } else {
            log.debug("No new avatar file provided or file was empty for user {}. Skipping avatar update logic.", user.getId());
        }

        if (updated) {
            userRepository.save(user);
            log.info("User {} entity saved after personal info update.", user.getId());
        } else {
            log.info("No changes detected for user {} personal info. Skipping save.", user.getId());
        }
        BasicPersonalInfoDTO personalInfo = BasicPersonalInfoDTO.builder()
                .fullName(personal.getFullName())
                .email(user.getContactInfo().getEmail())
                .phoneNumber(contact.getPhoneNumber())
                .address(contact.getAddress())
                .dateOfBirth(personal.getDateOfBirth())
                .gender(personal.getGender())
                .nationality(personal.getNationality())
                .build();

        String message = updated ? "Personal information updated successfully!" : "No changes detected.";
        return PostPersonalInfoResponse.success(message, personalInfo, avatarUrl);
    }

    @PreAuthorize("@userSecurityService.isOwnerById(#id, authentication.principal.id)")
    @Override
    public PutEducationResponse updateEducation(long id, PutEducationRequest request) {
        User currentUser = getCurrentUser();

        Education educationToUpdate = educationRepository.findById(id)
                .orElseThrow(() -> new EducationNotFoundException(
                        "Cant find education with id: " + id));

        // Lấy tất cả các mục học vấn khác của người dùng để kiểm tra trùng lặp
        List<Education> otherEducations = educationRepository.findByUser(currentUser).stream()
                .filter(edu -> !edu.getId().equals(id))
                .collect(Collectors.toList());

        educationToUpdate.updateFromRequest(
                request,
                otherEducations
        );

        Education savedEducation = educationRepository.save(educationToUpdate);

        return PutEducationResponse.fromEducation(
                savedEducation,
                "Education information updated successfully."
        );
    }

    @Override
    public PostEducationResponse saveEducation(PostEducationRequest request) {
        // 1. Get current user
        User currentUser = getCurrentUser();

        List<Education> educationToUpdate = educationRepository.findByUser(currentUser);

        // 2. Create entity using factory method
        // Truyền danh sách đã có vào để entity tự validate
        Education education = Education.createFromRequest(request, currentUser, educationToUpdate);

        // 3. Save to database
        Education savedEducation = educationRepository.save(education);

        // 4. Convert to response using entity method
        return savedEducation.toResponseDTO();
    }

    @Override
    @Transactional
    @PreAuthorize("@userSecurityService.isOwnerById(#educationId, authentication.principal.id)")
    public void deleteEducation(Long educationId) {
        log.info("Attempting to delete education with ID: {}", educationId);
        if (!educationRepository.existsById(educationId)) {
            throw new EducationNotFoundException("Education with ID " + educationId + " not found.");
        }
        educationRepository.deleteById(educationId);
        log.info("Successfully deleted education with ID: {}", educationId);
    }

    @Override
    @Transactional
    @PreAuthorize("@userSecurityService.isJobExperienceOwner(#jobExperienceId, authentication.principal.id)")
    public void deleteJobExperience(Long jobExperienceId) {
        log.info("Attempting to delete job experience with ID: {}", jobExperienceId);
        if (!jobExperienceRepository.existsById(jobExperienceId)) {
            throw new JobExperienceNotFoundException();
        }
        jobExperienceRepository.deleteById(jobExperienceId);
        log.info("Successfully deleted job experience with ID: {}", jobExperienceId);
    }

    @Override
    public PostJobExperienceResponse createJobExperienceForCurrentUser(PostJobExperienceRequest request) {
        User currentUser = getCurrentUser();

        // Lấy danh sách các kinh nghiệm đã có để kiểm tra trùng lặp
        List<JobExperience> existingJobs = jobExperienceRepository.findByUser(currentUser);

        JobExperience jobExperience = JobExperience.create(
                currentUser,
                request.jobTitle(),
                request.companyName(),
                request.startDate(),
                request.endDate(),
                request.description(),
                existingJobs // Truyền danh sách vào
        );

        JobExperience saved = jobExperienceRepository.save(jobExperience);

        return new PostJobExperienceResponse(
                saved.getId(),
                saved.getJobTitle(),
                saved.getCompanyName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getDescription()
        );
    }

    @Override
    @PreAuthorize("@userSecurityService.isJobExperienceOwner(#id, authentication.principal.id)")
    public PutJobExperienceResponse updateJobExperience(Long id, PutJobExperienceRequest request) {
        User currentUser = getCurrentUser();

        JobExperience jobExperienceToUpdate = jobExperienceRepository.findById(id)
                .orElseThrow(() -> new JobExperienceNotFoundException());

        // Lấy danh sách các kinh nghiệm khác để kiểm tra trùng lặp
        // Entity sẽ tự lọc ra chính nó, nên ta chỉ cần truyền toàn bộ danh sách
        List<JobExperience> otherJobs = jobExperienceRepository.findByUser(currentUser);

        jobExperienceToUpdate.updateFromRequest(
                request.jobTitle(),
                request.companyName(),
                request.startDate(),
                request.endDate(),
                request.description(),
                otherJobs // Truyền danh sách vào
        );

        JobExperience savedJobExperience = jobExperienceRepository.save(jobExperienceToUpdate);
        return PutJobExperienceResponse.from(savedJobExperience);
    }

    @Override
    @Transactional
    public PostApplyJobResponse applyForJob(Long jobPostingId, PostApplyJobRequest request) {
        // 1. Load job posting
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new JobNotFoundExceptionJson("Job posting not found."));

        User user = getCurrentUser();

        // 2. ✅ Sử dụng validation service
        jobApplicationValidationService.executeAllValidations(jobPosting, user);

        // 3. Handle job expiry - update status và publish event
        if (jobPosting.isExpired() && jobPosting.needsStatusUpdate()) {
            jobPosting.expire();

            applicationEventPublisher.publishEvent(
                    new JobPostingExpiredEvent(
                            jobPosting.getId(),
                            jobPosting.getTitle(),
                            jobPosting.getCompany().getId(),
                            jobPosting.getCompany().getContactEmail(),
                            jobPosting.getCompany().getName(),
                            ExpireReason.DEADLINE_PASSED
                    )
            );
            jobPostingRepository.save(jobPosting);
        }

        // 4. Upload CV file
        String resumeFilePath = fileService.uploadCV(request.cvUpload(), user.getId());

        // 5. Create application entity
        Applicant newApplicant = Applicant.createNewApplication(
                user,
                jobPosting,
                resumeFilePath,
                request.coverLetter()
        );

        // 6. Save to database
        Applicant savedApplicant = applicantRepository.save(newApplicant);

        // 7. === TĂNG RECEIVED COUNT === (Logic mới thêm)
        // Gọi method trong entity, Hibernate tự dirty check và update
        jobPosting.onNewApplicationReceived();

        // 8. Gọi company ra khỏi jobposting
        Company employer = jobPosting.getCompany();

        boolean isEmailEnabled = false;
        if (employer.getCompanySetting() != null) {
            isEmailEnabled = employer.getCompanySetting().isEmailOnNewApplicant();
        }

        // 9. Publish ApplicationSubmittedEvent
        applicationEventPublisher.publishEvent(new ApplicationSubmittedEvent(
                savedApplicant.getId(),
                employer.getId(),
                employer.getUsername(),
                user.getPersonalInfo().getFullName(),
                user.getProfilePicture(),
                jobPosting.getTitle(),
                employer.getContactEmail(),
                employer.getName(),
                employer.getCompanySetting().isEmailOnNewApplicant()
        ));

        // 9. Return response
        return PostApplyJobResponse.success(
                savedApplicant.getId(),
                "/jobseeker/my-application?highlightApplicantId=" + savedApplicant.getId()
        );
    }


    @Transactional(readOnly = true)
    public GetMyApplicantResponse getMyApplicants(User currentUser, Long notiId, Long highlightId) {

        // =================================================================
        // BƯỚC 1: LẤY VỎ (Query 1)
        // =================================================================
        // Lúc này 'cards' chỉ có thông tin cơ bản, list history bên trong đang RỖNG
        List<GetMyApplicantResponse.ApplicantCard> cards = applicantRepository.findBasicCards(currentUser);

        // Check nhanh: Nếu chưa apply gì thì về luôn
        if (cards.isEmpty()) {
            return new GetMyApplicantResponse(Collections.emptyList(), null);
        }

        // =================================================================
        // BƯỚC 2: LẤY NHÂN (Query 2)
        // =================================================================
        // Lấy list ID để query lịch sử
        List<Long> applicantIds = cards.stream()
                .map(GetMyApplicantResponse.ApplicantCard::id)
                .toList();

        // Lấy toàn bộ lịch sử của các ID trên
        List<GetMyApplicantResponse.StatusHistory> histories = applicantRepository.findHistoriesByApplicantIds(applicantIds);

        // =================================================================
        // BƯỚC 3: CẦU NỐI (Logic ghép cặp) - QUAN TRỌNG NHẤT
        // =================================================================

        // 3.1. Gom nhóm lịch sử vào Map để tra cứu cho nhanh
        // Key: applicantId -> Value: List History của id đó
        Map<Long, List<GetMyApplicantResponse.StatusHistory>> historyMap = histories.stream()
                .collect(Collectors.groupingBy(GetMyApplicantResponse.StatusHistory::applicantId));

        // 3.2. Duyệt qua từng cái Card (Vỏ) để nhét History (Nhân) vào
        List<GetMyApplicantResponse.ApplicantCard> finalCards = cards.stream()
                .map(card -> {
                    // Lấy nhân tương ứng với vỏ (O(1) lookup)
                    List<GetMyApplicantResponse.StatusHistory> historyOfThisCard =
                            historyMap.getOrDefault(card.id(), Collections.emptyList());

                    // HÀM CẦU NỐI THỰC SỰ: withStatusHistory
                    // Tạo ra một bản copy của Card nhưng đã có History
                    return card.withStatusHistory(historyOfThisCard);
                })
                .toList();

        // =================================================================
        // BƯỚC 4: TRẢ VỀ
        // =================================================================
        Long finalHighlightId = (highlightId != null) ? highlightId : notiId;
        return new GetMyApplicantResponse(finalCards, finalHighlightId);
    }


    private <T> boolean updateField(Consumer<T> setter, T currentValue, T newValue) {
        if (!Objects.equals(currentValue, newValue)) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }

    @Transactional
    @Override
    public PostDeleteAccountFinalizeResponse finalizeAccountDeletion(String code) {

        // 1. Lấy người dùng hiện tại từ SecurityContext
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated for this operation.");
        }
        // 3. Xác thực mã OTP
        String userEmail = currentUser.getContactInfo().getEmail();
        emailService.verifyCode(userEmail, code);

        // 4. Thu thập thông tin cần thiết TRƯỚC KHI xóa
        List<String> filePaths = collectUserFilePaths(currentUser);

        // 5. Thực thi xóa người dùng
        userRepository.delete(currentUser);

        // 6. Bắn sự kiện SAU KHI xóa thành công (nhưng vẫn trong transaction)
        applicationEventPublisher.publishEvent(new UserAccountDeletedEvent(userEmail, filePaths));

        // 7. Trả về DTO chứa redirect URL
        return new PostDeleteAccountFinalizeResponse("/logout-action");
    }

    /**
     * Thu thập tất cả file paths cần xóa khi xóa user
     * - Loại trừ avatar mặc định
     * - Lấy resume từ tất cả applications
     */
    private List<String> collectUserFilePaths(User user) {
        List<String> filePaths = new ArrayList<>();

        // 1. Avatar - KIỂM TRA không phải là default
        if (user.getProfilePicture() != null &&
                !user.getProfilePicture().equals(User.DEFAULT_PROFILE_PICTURE)) {
            filePaths.add(user.getProfilePicture());
        }

        // 2. Resume files từ tất cả applications
        if (user.getApplicants() != null && !user.getApplicants().isEmpty()) {
            user.getApplicants().stream()
                    .map(Applicant::getResume)
                    .filter(resume -> resume != null && !resume.trim().isEmpty())
                    .forEach(filePaths::add);
        }

        return filePaths;
    }

    /**
     * Validation 2: Check if user profile is complete
     * Throws: ProfileIncompleteException
     */
    private CompletableFuture<Void> validateProfileCompletion(User user) {
        return CompletableFuture.runAsync(() -> {
            log.debug("[JOB_APPLICATION] [PROFILE_CHECK] Checking profile completeness...");

            if (!user.isProfileComplete()) {
                log.error("[JOB_APPLICATION] [PROFILE_CHECK] ❌ Profile incomplete");
                throw new RuntimeException("ProfileIncomplete");
            }

            log.debug("[JOB_APPLICATION] [PROFILE_CHECK] ✅ Profile check passed");
        });
    }

    /**
     * Validation 3: Check if user has already applied for this job
     * Throws: DuplicateApplicationException
     */
    private CompletableFuture<Optional<Applicant>> validateNoDuplicateApplication(User user, JobPosting jobPosting) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("[JOB_APPLICATION] [DUPLICATE_CHECK] Checking for duplicate application...");

            Optional<Applicant> existing = applicantRepository.findExistingApplication(
                    user.getId(),
                    jobPosting.getId()
            );

            if (existing.isPresent()) {
                log.error("[JOB_APPLICATION] [DUPLICATE_CHECK] ❌ Duplicate found - ID: {}",
                        existing.get().getId());
                throw new RuntimeException("DuplicateApplication:" + existing.get().getId());
            }

            log.debug("[JOB_APPLICATION] [DUPLICATE_CHECK] ✅ No duplicate found");
            return existing;
        });
    }

    /**
     * Validation 4: Check if job posting has reached max applicant limit
     * Throws: ApplicationLimitReachedException
     */


    @Override
    public List<User> findAllByUsernameIn(List<String> usernames) {
        if (usernames == null || usernames.isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Fetching {} users by usernames in a single query.", usernames.size());
        return userRepository.findAllByUsernameIn(usernames);
    }

    @Override
    public void initiatePasswordReset(String username) throws MessagingException {
        // ✅ IMPROVED: Search for both User and Company accounts.

        // 1. Try to find a 'User' account first.
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getContactInfo() != null && user.getContactInfo().getEmail() != null) {
                log.info("Initiating password reset for user: {}. Sending code to email.", username);
                emailService.sendVerification(user.getContactInfo().getEmail(), VerificationType.FORGET_PASSWORD);
                return; // Found and sent, process is complete.
            }
        }

        // 2. If not found as a User, try to find a 'Company' account.
        Optional<Company> companyOptional = companyRepository.findByUsername(username);
        if (companyOptional.isPresent()) {
            Company company = companyOptional.get();
            if (company.getContactEmail() != null && !company.getContactEmail().isEmpty()) {
                log.info("Initiating password reset for company account: {}. Sending code to email.", username);
                emailService.sendVerification(company.getContactEmail(), VerificationType.FORGET_PASSWORD);
                return; // Found and sent, process is complete.
            }
        } else {
            log.warn("Password reset requested for non-existent user/company: {}. No action taken.", username);
        }

        // 3. If neither account type is found, or if found but has no email, throw an exception.
        throw new UsernameNotFoundException("No account with a registered email was found for username: " + username);
    }

    @Override
    public void verifyPasswordResetCode(PostVerifyEmailRequest request, HttpSession session) {
        // 1. ✅ IMPROVED: Use the new method to find email from either User or Company.
        String accountEmail = findEmailByUsername(request.getUsername());
        if (accountEmail == null) {
            throw new UsernameNotFoundException("Account not found or email is missing for username: " + request.getUsername());
        }

        // 2. Gọi EmailService để xác thực mã.
        // Phương thức này sẽ ném InvalidVerificationCodeException nếu mã sai hoặc hết hạn
        emailService.verifyCode(accountEmail, request.getEmailVerificationCode());

        // 3. Nếu mã đúng, tạo một phiên làm việc có thời hạn (5 phút)
        long currentTime = System.currentTimeMillis();
        long expiryTime = currentTime + (passwordResetSessionDurationMinutes * 60 * 1000L);

        session.setAttribute("forgetPasswordVerified_" + request.getUsername(), true);
        session.setAttribute("forgetPasswordExpiryTime_" + request.getUsername(), expiryTime);

        log.info("Password reset code verified for user: {}. Session created for {} minutes.", request.getUsername(), passwordResetSessionDurationMinutes);
    }
}
