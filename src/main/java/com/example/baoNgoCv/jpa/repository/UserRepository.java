package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.valueObject.SocialLink;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.projection.jobPosting.UserForApplyJobProjection;
import com.example.baoNgoCv.jpa.projection.user.BasicProfileResponse;
import com.example.baoNgoCv.model.enums.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByContactInfo_Email(String email);

    @Modifying
    @Query(value = "DELETE FROM company_followers WHERE user_id = :userId", nativeQuery = true)
    void deleteFromCompanyFollowers(@Param("userId") Long userId);

    @Modifying
    @Query(value = "DELETE FROM user_permissions WHERE user_id = :userId", nativeQuery = true)
    void deleteFromUserPermissions(@Param("userId") Long userId);

    @Query("""
        SELECT new com.example.baoNgoCv.jpa.projection.user.BasicProfileResponse(
            u.id,
            u.username,
            u.profilePicture,
            u.personalInfo.fullName,
            u.personalInfo.dateOfBirth,
            u.personalInfo.gender,
            u.personalInfo.nationality,
            u.contactInfo.email,
            u.contactInfo.phoneNumber,
            u.contactInfo.address,
            u.auditInfo.createdAt,
            u.auditInfo.updatedAt,
            COALESCE(us.profilePublic, false)
        )
        FROM User u
        LEFT JOIN u.userSettings us
        WHERE u.id = :userId
    """)
    Optional<BasicProfileResponse> findBasicProfileById(@Param("userId") Long userId);

    @Query("""
    SELECT u.id AS id, 
           u.username AS username, 
           u.personalInfo.fullName AS fullName, 
           u.contactInfo.email AS email, 
           u.contactInfo.phoneNumber AS phoneNumber, 
           u.profilePicture AS profilePicture 
    FROM User u 
    WHERE u.id = :userId
""")
    Optional<UserForApplyJobProjection> findUserProjection(@Param("userId") Long userId);

    @Query("SELECT s FROM User u JOIN u.skills s WHERE u.id = :userId")
    List<Skill> findSkillsByUserId(@Param("userId") Long userId);

    @Query("SELECT sl FROM User u JOIN u.socialLinks sl WHERE u.id = :userId")
    List<SocialLink> findSocialLinksByUserId(@Param("userId") Long userId);

    boolean existsByUsername(String username);

    List<User> findAllByUsernameIn(List<String> usernames);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE username = :username UNION ALL SELECT 1 FROM company WHERE username = :username)", nativeQuery = true)
    Integer existsByUsernameInUsersOrCompanies(@Param("username") String username);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE email = :email UNION ALL SELECT 1 FROM company WHERE contact_email = :email)", nativeQuery = true)
    Integer existsByEmailInUsersOrCompanies(@Param("email") String email);
}
