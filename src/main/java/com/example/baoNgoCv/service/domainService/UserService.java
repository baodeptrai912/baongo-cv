package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.NotificationSettingsDto;
import com.example.baoNgoCv.model.dto.PersonalInforUpdateDTO;
import com.example.baoNgoCv.model.dto.applicant.GetMyApplicantResponse;

import com.example.baoNgoCv.model.dto.common.VerifyPasswordRequest;
import com.example.baoNgoCv.model.dto.user.*;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.Skill;
import com.example.baoNgoCv.model.session.PendingUserRegistration;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    User findByUsername(String username);

    User save(User user);

    Optional<User> findByEmail(String email);

    User getCurrentUser();

    User getCurrentUserOrNull();

    Company getCurrentCompany();

    Boolean checkPassword(User user, String currentPassword);

    Boolean isPhoneNumber(long id);


    boolean isUserProfileComplete(User user);

    void addFollower(Long companyId);

    void removeFollower(Long companyId);

    void deleteUser(Long id);

    GetProfileResponse getProfileData();

    GetProfileUpdateResponse getProfileUpdateData();

    PostPersonalInfoResponse updatePersonalInfo(PersonalInforUpdateDTO personalInforUpdateDTO
                                                );
    PutEducationResponse updateEducation(long id, PutEducationRequest request);

    PostEducationResponse saveEducation(PostEducationRequest request);

    PostJobExperienceResponse createJobExperienceForCurrentUser(PostJobExperienceRequest request);

    void deleteEducation(Long educationId);

    void deleteJobExperience(Long jobExperienceId);

    PutJobExperienceResponse updateJobExperience(Long id, PutJobExperienceRequest request);

    PostApplyJobResponse applyForJob(Long jobPostingId,  PostApplyJobRequest request);

    GetMyApplicantResponse getMyApplicants(User currentUser, Long notiId, Long highlightId);

    void processPasswordChangeAndInvalidateSessions(String username, String newPassword);

    boolean existsByUsername(String username);

    void completeRegistration(PendingUserRegistration pendingRegistration);

    void updateSocialLinks(List<PostSocialLinkRequest> socialLinksRequest);



    PostUpdateSkillResponse updateSkills(List<Skill> skills);

    PostVerifyPasswordForDeletionResponse verifyPasswordToDeleteAccount(VerifyPasswordRequest request);

    PostDeleteAccountFinalizeResponse finalizeAccountDeletion(String code);

    List<User> findAllByUsernameIn(List<String> usernames);

    void initiatePasswordReset(String username) throws MessagingException;

    void verifyPasswordResetCode(PostVerifyEmailRequest request, HttpSession session);

    void updateNotificationSettings(String username, boolean emailOnUpdate);

    void updatePrivacySettings(String username, boolean isPublic);

    String findEmailByUsername(String username);

    Optional<User> getUserById(Long userId);

    UserProfileView getProfileForViewing(Long userId);
}
