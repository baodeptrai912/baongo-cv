package com.example.baoNgoCv.jpa.projection.company;

import com.example.baoNgoCv.model.enums.IndustryType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object (DTO) được thiết kế riêng để chứa thông tin chi tiết của công ty,
 * tối ưu cho việc nhận dữ liệu từ các câu lệnh JPA projection.
 */
@Getter
@Setter
@NoArgsConstructor // Cần thiết để các thư viện như Jackson có thể hoạt động
@ToString
public class CompanyDetailDTO {

    private Long id;
    private String name;
    private String description;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private String website;
    private String companyLogo;
    private LocalDateTime createdAt;
    private Integer companySize;
    private IndustryType industry;

    // SỬA LỖI: Đã đổi sang Long để khớp với kết quả của hàm SIZE() trong JPQL
    private Long followersCount;

    // Các trường được tính toán (Computed fields) để giảm tải cho frontend
    private String formattedCreatedAt;
    private String industryDisplayName;

    /**
     * Constructor này được thiết kế đặc biệt cho JPA Constructor Expression.
     * Chữ ký (số lượng và kiểu) của các tham số phải khớp chính xác
     * với các trường được SELECT trong câu lệnh @Query của Repository.
     */
    public CompanyDetailDTO(Long id,                    // c.id
                            String name,                // c.name
                            String description,         // c.description
                            String location,            // c.location
                            String contactEmail,        // c.contactEmail
                            String contactPhone,        // c.contactPhone
                            String website,             // c.website
                            String companyLogo,         // c.companyLogo
                            LocalDateTime createdAt,    // c.createdAt
                            Integer companySize,        // c.companySize
                            IndustryType industry,      // c.industry
                            Long followersCount) {      // SIZE(c.followers) - trả về Long
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.website = website;
        this.companyLogo = companyLogo;
        this.createdAt = createdAt;
        this.companySize = companySize;
        this.industry = industry;

        // SỬA LỖI: Gán giá trị Long một cách an toàn, không cần ép kiểu
        this.followersCount = followersCount != null ? followersCount : 0L;

        // Tự động tính toán các trường bổ sung sau khi các giá trị chính được gán
        this.formattedCreatedAt = formatCreatedAt(createdAt);
        this.industryDisplayName = industry != null ? industry.getDisplayName() : "Chưa cập nhật"; // Ví dụ: dùng getDisplayName() từ Enum
    }

    /**
     * Phương thức private helper để định dạng ngày tháng.
     * @param createdAt Thời gian cần định dạng.
     * @return Chuỗi ngày tháng đã được định dạng hoặc null.
     */
    private String formatCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        // Khai báo formatter một lần để tái sử dụng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return createdAt.format(formatter);
    }
}