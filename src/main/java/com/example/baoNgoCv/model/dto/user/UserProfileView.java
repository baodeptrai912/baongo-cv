package com.example.baoNgoCv.model.dto.user;

import com.example.baoNgoCv.model.entity.Education;
import com.example.baoNgoCv.model.entity.JobExperience;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.enums.Skill;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO này đại diện cho dữ liệu được hiển thị trên trang hồ sơ công khai của người dùng.
 * Nó được thiết kế để linh hoạt, có thể hiển thị đầy đủ hoặc một phần thông tin
 * tùy thuộc vào cài đặt riêng tư của người dùng.
 */
@Builder
public record UserProfileView(
        // 1. Thông tin chung (Luôn có)
        String fullName,
        String avatar,
        String bio,
        String email,
        String phoneNumber,
        // 2. Cờ trạng thái (Quan trọng để View hiển thị biểu tượng khóa)
        boolean isPrivate,

        // 3. Thông tin chi tiết (Có thể rỗng nếu private)
        List<EducationItem> educations,
        List<ExperienceItem> experiences,
        Set<Skill> skills
) {

    /**
     * DTO con cho mục học vấn.
     */
    @Builder
    public record EducationItem(
            String degree,
            String institution,
            LocalDate startDate,
            LocalDate endDate
    ) {
        public static EducationItem fromEntity(Education education) {
            return new EducationItem(
                    education.getDegree(),
                    education.getInstitution(),
                    education.getStartDate(),
                    education.getEndDate()
            );
        }
    }

    /**
     * DTO con cho mục kinh nghiệm làm việc.
     */
    @Builder
    public record ExperienceItem(
            String jobTitle,
            String companyName,
            LocalDate startDate,
            LocalDate endDate,
            String description
    ) {
        public static ExperienceItem fromEntity(JobExperience experience) {
            return new ExperienceItem(
                    experience.getJobTitle(),
                    experience.getCompanyName(),
                    experience.getStartDate(),
                    experience.getEndDate(),
                    experience.getDescription()
            );
        }
    }

    /**
     * Factory method để tạo DTO từ User entity, xử lý logic private/public.
     */
    public static UserProfileView fromUser(User user) {
        boolean isPublic = user.getUserSettings() != null && user.getUserSettings().isProfilePublic();

        return UserProfileView.builder()
                .fullName(user.getPersonalInfo().getFullName())
                .avatar(user.getProfilePicture())
                .bio("Building feature =))")
                .phoneNumber(user.getContactInfo().getPhoneNumber())
                .email(user.getContactInfo() != null ? user.getContactInfo().getEmail() : null)
                .isPrivate(!isPublic)
                .educations(isPublic ? user.getEducations().stream().map(EducationItem::fromEntity).collect(Collectors.toList()) : Collections.emptyList())
                .experiences(isPublic ? user.getJobExperiences().stream().map(ExperienceItem::fromEntity).collect(Collectors.toList()) : Collections.emptyList())
                .skills(isPublic ? user.getSkills() : Collections.emptySet())
                .build();
    }
}