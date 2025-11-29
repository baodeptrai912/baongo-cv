package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.exception.emailException.InvalidVerificationCodeException;
import com.example.baoNgoCv.exception.registrationException.DuplicateRegistrationDataException;

import com.example.baoNgoCv.model.dto.ProfileVisibilityUpdateDTO;
import com.example.baoNgoCv.model.dto.common.ApiResponse;
import com.example.baoNgoCv.model.dto.common.PasswordChangeRequest;
import com.example.baoNgoCv.model.dto.company.PostRegisterRequest;
import com.example.baoNgoCv.model.dto.user.*;
import com.example.baoNgoCv.model.enums.NotificationType;
import com.example.baoNgoCv.exception.registrationException.RegistrationEmailMismatchException;
import com.example.baoNgoCv.exception.registrationException.RegistrationSessionExpiredException;
import com.example.baoNgoCv.exception.securityException.InvalidPasswordResetSessionException;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Notification;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.repository.NotificationRepository;
import com.example.baoNgoCv.jpa.repository.PermissionRepository;
import com.example.baoNgoCv.model.enums.VerificationType;
import com.example.baoNgoCv.service.domainService.CompanyService;
import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.domainService.UserServiceImpl;
import com.example.baoNgoCv.service.domainService.UserSettingsService;
import com.example.baoNgoCv.model.session.PendingCompanyRegistration;
import com.example.baoNgoCv.service.securityService.PasswordResetValidationService;
import com.example.baoNgoCv.service.utilityService.EmailService;
import com.example.baoNgoCv.service.utilityService.FileService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import com.example.baoNgoCv.model.session.PendingUserRegistration;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/user")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final PendingCompanyRegistration pendingCompanyRegistration;
    private final PendingUserRegistration pendingUserRegistration;
    @Value("${app.password-reset.session-duration-minutes:5}")
    private int passwordResetSessionDurationMinutes;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationRepository notificationRepository;
    private final FileService fileService;
    private final NotificationService notificationService;
    private final UserServiceImpl userServiceImpl;
    private final UserSettingsService userSettingsService;
    private final UserService userService;
    private final EmailService emailService;
    private final CompanyService companyService;
    private final PasswordResetValidationService passwordResetValidationService;


    @GetMapping("/login")
    public String login(@RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                        @RequestParam(value = "error", required = false) String error,
                        HttpSession session,
                        Model model) {

        model.addAttribute("redirectUrl", redirectUrl);

        // Debug: In ra console xem cĂ³ username khĂ´ng
        if (error != null) {
            String lastUsername = (String) session.getAttribute("LAST_USERNAME");
            System.out.println("DEBUG - Last Username from session: " + lastUsername);

            if (lastUsername != null) {
                model.addAttribute("username", lastUsername);
                session.removeAttribute("LAST_USERNAME");
            }
        }

        return "/user/login";
    }

    @GetMapping("/register")
    public String register(Model model) {

        return "/user/register";
    }

    @GetMapping("/register/company")
    public String showCompanyRegistrationForm(Model model) {
        model.addAttribute("company", new PostRegisterRequest("", "", "", "", "", ""));
        return "company/company-register";
    }

    @GetMapping("/forget-password")
    public String forgetPassword() {
        // 1. Láº¥y thĂ´ng tin authentication tá»« SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 2. Kiá»ƒm tra user Ä‘Ă£ Ä‘Äƒng nháº­p chÆ°a (auth != null && authenticated && khĂ´ng pháº£i anonymous)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            // 3. User Ä‘Ă£ Ä‘Äƒng nháº­p tháº­t -> redirect vá» trang chá»§
            return "redirect:/main/home";
        }

        // 4. User chÆ°a Ä‘Äƒng nháº­p -> hiá»ƒn thá»‹ form forgot password
        return "/user/forget-password";
    }


    @InitBinder
    public void initBinder(WebDataBinder data) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        data.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerAction(@Valid @ModelAttribute com.example.baoNgoCv.model.dto.user.PostRegisterRequest postRegisterRequest) throws MessagingException {

        // 1. Perform business logic
        emailService.sendVerification(postRegisterRequest.getEmail(), VerificationType.REGISTRATION);
        pendingUserRegistration.storeRegistrationData(postRegisterRequest);

        // 2. Prepare data payload for the response
        Map<String, Object> data = new HashMap<>();
        data.put("email", postRegisterRequest.getEmail());

        // 3. Create a standardized ApiResponse
        ApiResponse<Map<String, Object>> apiResponse = ApiResponse.success(data, "A 6-digit verification code has been sent to your email.");

        // 4. Return the standardized response
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register/company/initiate")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> initiateCompanyRegistration(@Valid @RequestBody PostRegisterRequest request, BindingResult bindingResult) throws MessagingException {
        log.info("Attempting to register new company with username: {}", request.username());

        // Explicitly check for validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors for company registration: {}", bindingResult.getAllErrors());
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(ApiResponse.validationError("Please check your information and try again", errors));
        }

        // Kiá»ƒm tra dá»¯ liá»‡u trĂ¹ng láº·p trÆ°á»›c khi gá»­i email
        companyService.validateDuplicateInfo(request);

        // Gá»­i email xĂ¡c thá»±c
        emailService.sendVerification(request.contactEmail(), VerificationType.REGISTRATION);

        // LÆ°u thĂ´ng tin vĂ o session
        pendingCompanyRegistration.storeRegistrationData(request);

        // Tráº£ vá» thĂ´ng bĂ¡o yĂªu cáº§u xĂ¡c thá»±c email
        return ResponseEntity.ok(ApiResponse.success(Map.of("email", request.contactEmail()), "A verification code has been sent to your business email."));
    }

    @PostMapping("/register/company/verify")
    @ResponseBody
    public ResponseEntity<ApiResponse<?>> finalizeCompanyRegistration(@RequestParam String email, @RequestParam String code) {
        if (pendingCompanyRegistration.isEmpty() || !pendingCompanyRegistration.isEmailMatching(email)) {
            throw new RegistrationSessionExpiredException("Your registration session has expired or the email does not match. Please start over.");
        }

        emailService.verifyCode(email, code);

        companyService.registerNewCompany(pendingCompanyRegistration.getPendingRequest());
        pendingCompanyRegistration.clearPendingData();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Map.of("redirectUrl", "/user/login"), "Registration successful! Please log in."));
    }

    @PostMapping("/register/company/resend-code")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> resendCompanyVerificationCode(@RequestParam String email) throws MessagingException {
        // 1. Kiá»ƒm tra xem cĂ³ phiĂªn Ä‘Äƒng kĂ½ Ä‘ang chá» cho email nĂ y khĂ´ng
        if (pendingCompanyRegistration.isEmpty() || !pendingCompanyRegistration.isEmailMatching(email)) {
            throw new RegistrationSessionExpiredException("Your registration session has expired or the email does not match. Please start over.");
        }

        // 2. Kiá»ƒm tra giá»›i háº¡n táº§n suáº¥t gá»­i (EmailService Ä‘Ă£ tĂ­ch há»£p sáºµn)
        // vĂ  gá»­i láº¡i mĂ£
        emailService.sendVerification(email, VerificationType.REGISTRATION);

        // 3. Tráº£ vá» pháº£n há»“i thĂ nh cĂ´ng
        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent."));
    }


    /**
     * Completes the account creation process by verifying the email code.
     * This endpoint is the final step of the registration flow.
     * It relies on the GlobalExceptionHandler to handle all error scenarios.
     *
     * @param email            The user's email.
     * @param verificationCode The 6-digit code entered by the user.
     * @return A standardized ApiResponse indicating success and providing a redirect URL.
     */
    @PostMapping("/verify-email-for-registration")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyEmailForRegistration(@RequestParam("email") String email, @RequestParam("verificationCode") String verificationCode) {

        // 1. Pre-condition checks (Guards)
        if (pendingUserRegistration.isEmpty()) {
            throw new RegistrationSessionExpiredException("Your session is expired, please log in again !");
        }
        if (!pendingUserRegistration.isEmailMatching(email)) {
            log.warn("Email verification mismatch. Session email: {}, Request email: {}", pendingUserRegistration.getEmail(), email);
            throw new RegistrationEmailMismatchException("Email is not match with the one sent to creat your account !");
        }

        // 2. Verify the code (throws InvalidVerificationCodeException on failure)
        emailService.verifyCode(email, verificationCode);

        // 3. Delegate account creation to the service
        userService.completeRegistration(pendingUserRegistration);

        // 4. Clear session data after successful registration
        pendingUserRegistration.clearPendingData();

        // 5. Prepare and return standardized success response
        Map<String, String> data = new HashMap<>();
        data.put("redirectUrl", "/user/login");

        return ResponseEntity.ok(ApiResponse.success(data, "Your account has been created successfully."));
    }

    /**
     * Resends the verification code to the user's email during registration.
     * This is triggered if the user did not receive the initial code or it expired.
     *
     * @param email The email to which the code should be resent.
     * @return A standardized ApiResponse indicating success or failure.
     */
    @PostMapping("/resend-verification-code")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(@RequestParam("email") String email) throws MessagingException {
        // Pre-condition check: Ensure there's a pending registration for this email
        // Step 1: Kiá»ƒm tra xem cĂ³ phiĂªn Ä‘Äƒng kĂ½ nĂ o Ä‘ang chá» xá»­ lĂ½ trong session khĂ´ng.
        if (pendingUserRegistration.isEmpty()) {
            throw new RegistrationSessionExpiredException("Your registration session has expired. Please start over.");
        }

        // Step 2: Kiá»ƒm tra báº£o máº­t, Ä‘áº£m báº£o email yĂªu cáº§u gá»­i láº¡i mĂ£ khá»›p vá»›i email trong phiĂªn.
        if (!pendingUserRegistration.isEmailMatching(email)) {
            log.warn("Email resend mismatch. Session email: {}, Request email: {}", pendingUserRegistration.getEmail(), email);
            throw new RegistrationEmailMismatchException("The provided email does not match the one in your registration session.");
        }

        // Step 3: Náº¿u táº¥t cáº£ kiá»ƒm tra Ä‘á»u qua, á»§y quyá»n cho EmailService Ä‘á»ƒ gá»­i má»™t mĂ£ má»›i.
        emailService.sendVerification(email, VerificationType.REGISTRATION);

        // Step 4: Tráº£ vá» pháº£n há»“i thĂ nh cĂ´ng cho client.
        return ResponseEntity.ok(ApiResponse.success("A new verification code has been sent."));
    }


    @GetMapping("/forget-password/final")
    public String forgetPasswordFinal(@RequestParam(value = "username", required = false) String username, Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        // âœ… ENHANCED: Guard against direct access without a username
        if (username == null || username.trim().isEmpty()) {
            log.warn("Direct access to /forget-password/final without a username. Redirecting to home.");
            redirectAttributes.addFlashAttribute("session_error_message", "Invalid access. Please start the password reset process again.");
            return "redirect:/main/home";
        }

        // âœ… TRĂCH NHIá»†M Cá»¦A CONTROLLER: Kiá»ƒm tra session
        boolean isSessionValid = passwordResetValidationService.isPasswordResetSessionValid(username, session);

        if (isSessionValid) {
            // TRĂCH NHIá»†M Cá»¦A CONTROLLER: Láº¥y dá»¯ liá»‡u tá»« session
            long remainingMs = passwordResetValidationService.getRemainingTimeMs(username, session);

            // Gá»i Ä‘áº¿n service Ä‘Ă£ Ä‘Æ°á»£c "lĂ m sáº¡ch"
            GetForgetPasswordFinalResponse pageData = passwordResetValidationService.createPasswordResetPageData(username, remainingMs);

            model.addAttribute("pageData", pageData);
            return "/user/forget-password-final";
        } else {
            // Náº¿u session khĂ´ng há»£p lá»‡, chuyá»ƒn hÆ°á»›ng
            log.warn("Invalid or expired password reset session access attempt for user: {}", username);
            redirectAttributes.addFlashAttribute("session_error_message", "Your password reset session has expired. Please start over.");
            return "redirect:/main/home";
        }
    }

    @PostMapping("/forget-password/final")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handlePasswordForgetFinal(@RequestBody PostForgetPasswordFinalRequest request, HttpSession session) {

        // Simple happy path - let service handle all validation
        passwordResetValidationService.processPasswordReset(request, session);

        // Success response theo ApiResponse format
        ApiResponse<Void> response = ApiResponse.success("You have changed your password successfully!");

        return ResponseEntity.ok(response);
    }


    /**
     * Verifies the 6-digit code sent to the user's email during the password reset process.
     */
    @PostMapping("/forget-password/verify-code")
    public ResponseEntity<ApiResponse<String>> verifyPasswordResetCode(@Valid @RequestBody PostVerifyEmailRequest request, HttpSession session) {
        // 1. á»¦y quyá»n toĂ n bá»™ logic cho UserService.
        // Service sáº½ xá»­ lĂ½ viá»‡c tĂ¬m ngÆ°á»i dĂ¹ng, xĂ¡c thá»±c mĂ£, vĂ  táº¡o session.
        // NĂ³ sáº½ nĂ©m ra exception náº¿u cĂ³ lá»—i, Ä‘Æ°á»£c xá»­ lĂ½ bá»Ÿi GlobalExceptionHandler.
        userService.verifyPasswordResetCode(request, session);

        // 2. Náº¿u khĂ´ng cĂ³ lá»—i, táº¡o URL chuyá»ƒn hÆ°á»›ng vĂ  tráº£ vá» pháº£n há»“i thĂ nh cĂ´ng.
        String redirectUrl = "/user/forget-password/final?username=" + request.getUsername();
        String message = "Code verified! You have 5 minutes to reset your password.";

        return ResponseEntity.ok(ApiResponse.success(redirectUrl, message));
    }


    @PostMapping("/forget-password/send-email-code")
    public ResponseEntity<ApiResponse<Void>> sendEmailCodeForgetPassword(@Valid @RequestBody PostEmailCodeRequest request, HttpSession session) throws MessagingException {

        // This ensures that if a user restarts the process, the old session is cleared,
        // preventing them from using an old, still-valid session.
        session.removeAttribute("forgetPasswordVerified_" + request.getUsername());
        session.removeAttribute("forgetPasswordExpiryTime_" + request.getUsername());

        // 1. Delegate the core logic to the UserService to send a new code.
        userService.initiatePasswordReset(request.getUsername());

        // 2. Return a standardized success response.
        ApiResponse<Void> response = ApiResponse.success("A verification code has been sent to your registered email address.");
        return ResponseEntity.ok(response);
    }


    @GetMapping("/get-current-user")
    public ResponseEntity<Map<String, Object>> getUser() {
        Map<String, Object> response = new HashMap<>();

        User user = userService.getCurrentUser();
        Company company = userService.getCurrentCompany();

        // Kiá»ƒm tra ngÆ°á»i dĂ¹ng
        if (user != null) {
            response.put("success", true);
            response.put("id", user.getId());
            response.put("userType", "user");
            return ResponseEntity.ok(response);
        } else if (company != null) {
            response.put("success", true);
            response.put("id", company.getId());
            response.put("userType", "company");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "User not found, please login first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam("username") String username) {
        Map<String, Object> response = new HashMap<>();
        User user = userService.findByUsername(username);

        if (user == null) {
            response.put("error", "The user does not exist");
            return ResponseEntity.ok(response);
        }

        response.put("phoneNumber", user.getContactInfo().getPhoneNumber() != null ? user.getContactInfo().getPhoneNumber() : "");
        response.put("email", user.getContactInfo().getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public String checkUsername() {


        return "test";
    }

    @PutMapping("/privacy")
    public ResponseEntity<?> updateUserProfileVisibility(@RequestBody ProfileVisibilityUpdateDTO dto, @AuthenticationPrincipal UserDetails userDetails) {

        System.out.println("--- [DEBUG] updateUserProfileVisibility Endpoint CALLED ---");

        if (dto != null) {
            System.out.println("[DEBUG] Request Body DTO - profileVisibility: " + dto.getProfileVisibility());
        } else {
            System.out.println("[DEBUG] Request Body DTO is NULL.");
        }

        if (userDetails == null) {
            System.err.println("[DEBUG] UserDetails is NULL. User not authenticated.");
            return ResponseEntity.status(401).body("User not authenticated");
        }

        String username = userDetails.getUsername();
        System.out.println("[DEBUG] Authenticated username: " + username);

        try {
            userSettingsService.updateProfileVisibility(username, dto);
            Map<String, String> response = new HashMap<>();

            System.out.println("[DEBUG] Success response prepared: " + response);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            System.err.println("[DEBUG] Caught IllegalArgumentException: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (UsernameNotFoundException e) {
            System.err.println("[DEBUG] Caught UsernameNotFoundException: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found.");
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            System.err.println("[DEBUG] Caught generic Exception: " + e.getClass().getName() + " - " + e.getMessage());
            // Äá»ƒ debug sĂ¢u hÆ¡n, báº¡n cĂ³ thá»ƒ in stack trace:
            // e.printStackTrace(System.err);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        } finally {
            System.out.println("--- [DEBUG] updateUserProfileVisibility Endpoint FINISHED ---");
        }

    }
}
