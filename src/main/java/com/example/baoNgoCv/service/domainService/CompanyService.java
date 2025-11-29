package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.exception.companyException.InvalidPasswordChangeException;
import com.example.baoNgoCv.exception.registrationException.DuplicateRegistrationDataException;
import com.example.baoNgoCv.model.dto.company.*;

import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.enums.IndustryType;
import com.example.baoNgoCv.model.enums.LocationType;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.Optional;

public interface CompanyService {
    void upgradeCurrentPlan(Long companyId);
    GetCompaniesResponse getCompaniesData(int page, String sort, String keyword, List<IndustryType> industries, LocationType location);
    Optional<Company> getById(long id);
    void followCompany(Long companyId, Long userId);
    boolean isFollowedByUser(Long companyId, Long userId);
    void unfollowCompany(Long companyId, Long userId);
    Optional<Company> findByName(String companyName);
    Optional<Company> findByUserName(String username);
    Optional<Company> findByEmail(String companyEmail);
    boolean deleteById(long id);
    Company save(Company company);
    PutInformationResponse updateCompanyProfile(Long companyId, PutInformationRequest request);
    PutContactResponse updateCompanyContact(Long companyId, PutContactRequest request);
    GetProfileUpdateResponse getProfileUpdateResponse(Long id);
    GetCompanyDetailResponse getCompanyDetailComplete(Long companyId);
    GetCompanyProfileResponse getCompanyProfile(Long id);
    boolean isOrderAlreadyProcessed(Long orderCode);
    boolean validateOrderAmount(Long orderCode, Integer amount);
    void downgradeExpiredAccounts();
    PasswordChangeInitResponse initiatePasswordChange(String currentPassword, String newPassword) throws InvalidPasswordChangeException, MessagingException;
    void finalizePasswordChange(String code, String pendingEmail, String newPassword) throws InvalidPasswordChangeException;
    void registerNewCompany(PostRegisterRequest request);

    void validateDuplicateInfo(PostRegisterRequest request) ;

    String initiateAccountDeletion( String password) throws InvalidPasswordChangeException, MessagingException;

    void finalizeAccountDeletion(String verificationCode) throws InvalidPasswordChangeException;
}
