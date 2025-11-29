package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.dto.user.PostEducationRequest;
import com.example.baoNgoCv.model.dto.user.PostEducationResponse;
import com.example.baoNgoCv.exception.educationException.InvalidEducationDateException;
import com.example.baoNgoCv.model.dto.user.PutEducationRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.example.baoNgoCv.exception.educationException.EducationOverlapException;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "degree", nullable = false)
    @NotBlank(message = "Degree is required")
    @Size(max = 255, message = "Degree must not exceed 255 characters")
    private String degree;

    @Column(name = "institution", nullable = false)
    @NotBlank(message = "Institution name is required")
    @Size(max = 255, message = "Institution name must not exceed 255 characters")
    private String institution;

    @Column(name = "start_date")
    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "graduated", nullable = false)
    private boolean graduated;

    @Column(name = "notes", length = 500)
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // =================================================================
    // CORE LOGIC: Dùng chung cho cả Create và Update
    // =================================================================
    private void applyEducationData(String degree, String institution,
                                    LocalDate startDate, LocalDate endDate,
                                    String notes, List<Education> otherEducations) {
        // 1. Check tuổi: Phải > 3 tuổi mới đi học
        validateStartDateAfterBirth(startDate);

        // 2. Check trùng lặp: Không được trùng với các Education khác
        // Lưu ý: List otherEducations phải được truyền từ Service vào
        if (otherEducations != null) {
            validateNoOverlap(startDate, endDate, otherEducations);
        }

        // 3. Gán dữ liệu
        this.degree = degree;
        this.institution = institution;
        this.startDate = startDate;
        this.endDate = endDate;
        this.notes = notes;
        this.graduated = calculateGraduationStatus(endDate);
    }

    // =================================================================
    // PUBLIC METHODS: Giao tiếp với bên ngoài
    // =================================================================

    /**
     * Tạo mới Education từ Request.
     * Bắt buộc truyền danh sách existingEducations để kiểm tra trùng lặp ngay từ đầu.
     */
    public static Education createFromRequest(PostEducationRequest request, User user, List<Education> existingEducations) {
        Education education = new Education();

        // Gán User cho đối tượng mới
        education.setUser(user);

        // Gọi hàm logic cốt lõi
        education.applyEducationData(
                request.degree(),
                request.institution(),
                request.startDate(),
                request.endDate(),
                request.notes(),
                existingEducations
        );

        return education;
    }

    /**
     * Cập nhật Education hiện tại từ Request.
     */
    public void updateFromRequest(PutEducationRequest request, List<Education> otherEducations) {
        // Gọi hàm logic cốt lõi (this chính là đối tượng đang được update)
        this.applyEducationData(
                request.degree(),
                request.institution(),
                request.startDate(),
                request.endDate(),
                request.notes(),
                otherEducations
        );
    }

    // =================================================================
    // VALIDATION LOGIC (Private)
    // =================================================================

    private void validateNoOverlap(LocalDate newStart, LocalDate newEnd, List<Education> otherEducations) {
        // Xử lý null cho newEnd (nếu đang học thì coi như MAX)
        LocalDate effectiveNewEnd = (newEnd == null) ? LocalDate.MAX : newEnd;

        for (Education other : otherEducations) {
            // Bỏ qua chính nó khi Update (tránh tự so sánh với mình)
            if (this.id != null && this.id.equals(other.getId())) {
                continue;
            }

            // Xử lý null cho other.getEndDate()
            LocalDate effectiveOtherEnd = (other.getEndDate() == null) ? LocalDate.MAX : other.getEndDate();

            // Logic kiểm tra trùng lặp chuẩn (bao gồm cả ngày biên và trường hợp Present vs Present)
            // Công thức: (Start A <= End B) VÀ (End A >= Start B)
            boolean isOverlapping = !newStart.isAfter(effectiveOtherEnd) &&
                    !effectiveNewEnd.isBefore(other.getStartDate());

            if (isOverlapping) {
                String otherEndDisplay = (other.getEndDate() == null) ? "Present" : other.getEndDate().toString();
                throw new EducationOverlapException(
                        String.format("Education period overlaps with another entry: %s (%s - %s)",
                                other.getDegree(),
                                other.getStartDate(),
                                otherEndDisplay)
                );
            }
        }
    }

    private void validateStartDateAfterBirth(LocalDate startDate) {
        // Kiểm tra null an toàn
        if (this.user == null ||
                this.user.getPersonalInfo() == null ||
                this.user.getPersonalInfo().getDateOfBirth() == null) {
            return;
        }

        LocalDate dateOfBirth = this.user.getPersonalInfo().getDateOfBirth();

        // Quy định: Phải ít nhất 3 tuổi mới được nhập học
        int MIN_AGE_TO_START_SCHOOL = 3;
        LocalDate minSchoolDate = dateOfBirth.plusYears(MIN_AGE_TO_START_SCHOOL);

        // Logic: Ngày nhập học phải >= Sinh nhật 3 tuổi
        if (startDate != null && startDate.isBefore(minSchoolDate)) {
            throw new InvalidEducationDateException(
                    String.format(
                            "Invalid education start date. User was born on %s and must be at least %d years old to start education. Earliest valid date is %s.",
                            dateOfBirth,
                            MIN_AGE_TO_START_SCHOOL,
                            minSchoolDate
                    )
            );
        }
    }

    private boolean calculateGraduationStatus(LocalDate endDate) {
        if (endDate == null) {
            return false; // Đang học -> Chưa tốt nghiệp
        }
        LocalDate now = LocalDate.now();
        return !endDate.isAfter(now); // Đã kết thúc (<= now) -> Tốt nghiệp
    }

    // =================================================================
    // UTILS / DTO CONVERSION
    // =================================================================

    public PostEducationResponse toResponseDTO() {
        return new PostEducationResponse(
                this.id,
                this.degree,
                this.institution,
                this.startDate,
                this.endDate,
                this.notes
        );
    }

    public boolean isCurrentEducation() {
        if (this.endDate == null) {
            return true;
        }
        return this.endDate.isAfter(LocalDate.now());
    }

    public String getDurationText() {
        String start = this.startDate != null ? this.startDate.toString() : "Unknown";
        String end = this.endDate != null ? this.endDate.toString() : "Present";
        return start + " - " + end;
    }
}
