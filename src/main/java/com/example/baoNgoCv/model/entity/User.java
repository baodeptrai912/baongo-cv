package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.valueObject.AuditInfo;
import com.example.baoNgoCv.model.valueObject.SocialLink;
import com.example.baoNgoCv.model.valueObject.ContactInfo;
import com.example.baoNgoCv.model.enums.Skill;
import com.example.baoNgoCv.model.valueObject.PersonalInfo;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "user")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User implements UserDetails {

    /**
     * Đường dẫn đến ảnh đại diện mặc định khi người dùng chưa cập nhật.
     */
    public static final String DEFAULT_PROFILE_PICTURE = "/img/default/defaultProfilePicture.jpg";

    /**
     * Khóa chính, định danh duy nhất cho mỗi người dùng.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Tên đăng nhập của người dùng, phải là duy nhất và không được null.
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Mật khẩu đã được mã hóa của người dùng.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * Đường dẫn URL đến ảnh đại diện của người dùng.
     * Có giá trị mặc định nếu người dùng chưa tải lên.
     */
    @Column(name = "profile_picture", nullable = true, length = 255)
    @Builder.Default
    private String profilePicture = DEFAULT_PROFILE_PICTURE;

    /**
     * Thông tin cá nhân của người dùng (Họ tên, ngày sinh, giới tính...).
     * Được nhúng trực tiếp vào bảng 'user'.
     */
    @Embedded
    @Builder.Default
    private PersonalInfo personalInfo = new PersonalInfo();

    /**
     * Thông tin liên lạc của người dùng (Email, số điện thoại).
     * Được nhúng trực tiếp vào bảng 'user'.
     */
    @Embedded
    private ContactInfo contactInfo = new ContactInfo();

    /**
     * Thông tin kiểm toán (ngày tạo, ngày cập nhật).
     * Được nhúng trực tiếp vào bảng 'user'.
     */
    @Embedded
    @Builder.Default
    private AuditInfo auditInfo = new AuditInfo();

    /**
     * Tập hợp các quyền (permissions) của người dùng.
     * Mối quan hệ Many-to-Many với entity {@link Permission}.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_permissions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Danh sách kinh nghiệm làm việc của người dùng.
     * Mối quan hệ One-to-Many với entity {@link JobExperience}.
     */
    @OneToMany(mappedBy = "user",cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<JobExperience> jobExperiences = new HashSet<>();

    /**
     * Danh sách quá trình học vấn của người dùng.
     * Mối quan hệ One-to-Many với entity {@link Education}.
     */
    @OneToMany(mappedBy = "user" ,cascade = {CascadeType.PERSIST, CascadeType.MERGE},  orphanRemoval = true)
    private Set<Education> educations = new HashSet<>();

    /**
     * Tập hợp các kỹ năng của người dùng.
     * Sử dụng {@link ElementCollection} để lưu danh sách các enum {@link Skill}
     * trong một bảng riêng biệt (`user_skills`).
     */
    @ElementCollection(targetClass = Skill.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Enumerated(EnumType.STRING)
    @Column(name = "skill", nullable = false, length = 50)
    private Set<Skill> skills = new HashSet<>();

    /**
     * Tập hợp các liên kết mạng xã hội của người dùng (LinkedIn, GitHub...).
     * Sử dụng {@link ElementCollection} để lưu các đối tượng {@link SocialLink}
     * trong một bảng riêng biệt (`user_social_links`).
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_social_links", joinColumns = @JoinColumn(name = "user_id"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<SocialLink> socialLinks = new HashSet<>();

    /**
     * Các công ty mà người dùng này đang theo dõi.
     * Mối quan hệ Many-to-Many, phía sở hữu là {@link Company}.
     */
    @ManyToMany(mappedBy = "followers")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Company> followedCompanies = new HashSet<>();

    /**
     * Các thông báo do người dùng này gửi đi.
     * Mối quan hệ One-to-Many với entity {@link Notification}.
     */
    @OneToMany(mappedBy = "senderUser", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Notification> notifications = new HashSet<>();

    /**
     * Các thông báo mà người dùng này nhận được.
     * Mối quan hệ One-to-Many với entity {@link Notification}.
     */
    @OneToMany(mappedBy = "recipientUser", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Notification> receivedNotifications = new HashSet<>();

    /**
     * Cài đặt riêng của người dùng.
     * Mối quan hệ One-to-One với entity {@link UserSettings}.
     */
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},fetch = FetchType.LAZY, orphanRemoval = true)
    private UserSettings userSettings;

    /**
     * Các công việc mà người dùng đã lưu.
     * Mối quan hệ One-to-Many với entity {@link JobSaved}.
     */
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},orphanRemoval = true)
    private Set<JobSaved> savedJobs = new HashSet<>();

    /**
     * Lịch sử các hoạt động đăng nhập của người dùng.
     * Mối quan hệ One-to-Many với entity {@link LoginActivity}.
     */
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},orphanRemoval = true)
    private Set<LoginActivity> loginActivities = new HashSet<>();

    /**
     * Các đơn ứng tuyển do người dùng này tạo ra.
     * Mối quan hệ One-to-Many với entity {@link Applicant}.
     */
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Applicant> applicants = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE},  orphanRemoval = true)
    private Set<JobAlert> jobAlerts = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Log trước khi xử lý
        System.out.println("🔍 [User.getAuthorities] Called for user: " + this.username);
        System.out.println("📦 [User.getAuthorities] Permissions count: " + (permissions != null ? permissions.size() : 0));

        if (permissions == null || permissions.isEmpty()) {
            System.out.println("⚠️  [User.getAuthorities] WARNING: No permissions found!");
            return new ArrayList<>();
        }

        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(permission -> {
                    String permName = permission.getName();
                    System.out.println("   🔑 Loading permission: " + permName);
                    return new SimpleGrantedAuthority(permName);
                })
                .collect(Collectors.toList());

        System.out.println("✅ [User.getAuthorities] Final authorities: " + authorities);
        return authorities;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }




    // --- Logic nghiệp vụ tùy chỉnh ---

    /**
     * Kiểm tra xem hồ sơ của người dùng đã hoàn chỉnh hay chưa.
     * @return {@code true} nếu hồ sơ hoàn chỉnh, ngược lại là {@code false}.
     */
    public boolean isProfileComplete() {
        return hasBasicInfo() &&
                hasContactInfo() &&
                hasExperienceOrEducation() &&
                hasProfilePicture();
    }

    private boolean hasBasicInfo() {
        return personalInfo != null &&
                personalInfo.getFullName() != null &&
                !personalInfo.getFullName().trim().isEmpty() &&
                personalInfo.getDateOfBirth() != null;
    }

    private boolean hasContactInfo() {
        return contactInfo != null &&
                contactInfo.getEmail() != null &&
                !contactInfo.getEmail().trim().isEmpty() &&
                contactInfo.getPhoneNumber() != null &&
                !contactInfo.getPhoneNumber().trim().isEmpty();
    }

    private boolean hasExperienceOrEducation() {
        return (jobExperiences != null && !jobExperiences.isEmpty()) ||
                (educations != null && !educations.isEmpty());
    }

    private boolean hasProfilePicture() {
        return profilePicture != null && !profilePicture.trim().isEmpty();
    }

    /**
     * Vòng đời JPA callback.
     * Được gọi tự động ngay trước khi một thực thể User được lưu lần đầu tiên.
     * Đảm bảo các đối tượng nhúng không bao giờ là null khi được lưu vào cơ sở dữ liệu.
     */
    @PrePersist
    protected void initializeDefaults() {

        if (this.personalInfo == null) {
            this.personalInfo = new PersonalInfo();
        }
        if (this.contactInfo == null) {
            this.contactInfo = new ContactInfo();
        }
    }

    /**
     * Static factory method để tạo một User mới từ dữ liệu đăng ký.
     * Đóng gói logic khởi tạo, đảm bảo User luôn được tạo ra một cách nhất quán.
     *
     * @param username        Tên đăng nhập.
     * @param rawPassword     Mật khẩu chưa mã hóa.
     * @param email           Địa chỉ email.
     * @param defaultPermission Quyền mặc định cho người dùng mới.
     * @param passwordEncoder   Đối tượng để mã hóa mật khẩu.
     * @return Một thực thể User mới, sẵn sàng để được lưu.
     */
    public static User createNew(String username, String rawPassword, String email, Permission defaultPermission, PasswordEncoder passwordEncoder) {
        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .contactInfo(ContactInfo.builder()
                        .email(email)
                        .build())
                .permissions(new HashSet<>(Set.of(defaultPermission)))
                .build();

        // ✅ IMPROVED: Luôn tạo UserSettings cùng với User
        // Điều này đảm bảo tính nhất quán và tránh NullPointerExceptions
        UserSettings settings = new UserSettings(newUser);
        newUser.setUserSettings(settings);

        return newUser;
    }

    // --- Helper methods for managing collections ---

    /**
     * Thêm một kỹ năng vào danh sách kỹ năng của người dùng.
     * @param skill Kỹ năng (enum) cần thêm.
     */
    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    /**
     * Xóa một kỹ năng khỏi danh sách kỹ năng của người dùng.
     * @param skill Kỹ năng (enum) cần xóa.
     */
    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
    }

    /**
     * Cập nhật toàn bộ danh sách kỹ năng của người dùng.
     * Xóa các kỹ năng cũ và thêm tất cả các kỹ năng từ tập hợp mới.
     * @param newSkills Tập hợp các kỹ năng mới.
     */
    public void updateAllSkills(Set<Skill> newSkills) {
        this.skills.clear();
        if (newSkills != null) {
            this.skills.addAll(newSkills);
        }
    }
    /**
     * Thêm một liên kết mạng xã hội cho người dùng.
     */
    public void updateAllSocialLinks(Set<SocialLink> newSocialLinks) {
        this.socialLinks.clear();
        if (newSocialLinks != null) {
            this.socialLinks.addAll(newSocialLinks);
        }
    }

    /**
     * Xóa một liên kết mạng xã hội của người dùng.
     * @param socialLink Đối tượng SocialLink cần xóa.
     */
    public void removeSocialLink(SocialLink socialLink) {
        this.socialLinks.remove(socialLink);
    }

    /**
     * ✅ IMPROVED: Helper method để đảm bảo liên kết hai chiều (bidirectional consistency).
     * Khi gán UserSettings cho User, cũng gán User này cho UserSettings.
     * @param userSettings Đối tượng cài đặt người dùng.
     */
    public void setUserSettings(UserSettings userSettings) {
        if (userSettings != null) {
            userSettings.setUser(this);
        }
        this.userSettings = userSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof User)) return false;
        User that = (User) o;

        return id != null && id.equals(that.id);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @PostLoad
    protected void ensureEmbeddedNotNull() {
        if (this.personalInfo == null) {
            this.personalInfo = new PersonalInfo();
        }
        if (this.contactInfo == null) {
            this.contactInfo = new ContactInfo();
        }
        if (this.auditInfo == null) {
            this.auditInfo = new AuditInfo();
        }
    }

}
