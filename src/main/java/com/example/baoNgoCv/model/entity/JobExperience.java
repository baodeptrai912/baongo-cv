package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.exception.jobExperienceException.JobExperienceOverlapException;
import com.example.baoNgoCv.exception.jobExperienceException.InvalidJobDateException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description")
    private String description;

    // =================================================================
    // CORE LOGIC: Hàm lõi dùng chung (Private)
    // =================================================================
    private void applyJobData(String jobTitle, String companyName,
                              LocalDate startDate, LocalDate endDate,
                              String description, List<JobExperience> otherJobs) {

        // 1. Validation: Ngày bắt đầu phải sau ngày sinh
        validateStartDateAfterBirth(startDate);

        // 2. Validation: Không được trùng lặp thời gian với job khác
        if (otherJobs != null) {
            validateNoOverlap(startDate, endDate, otherJobs);
        }

        // 3. Gán dữ liệu
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    // =================================================================
    // PUBLIC METHODS: Các lối vào
    // =================================================================

    /**
     * Tạo mới JobExperience (Static Factory Method)
     */
    public static JobExperience create(User user, String jobTitle, String companyName,
                                       LocalDate startDate, LocalDate endDate,
                                       String description, List<JobExperience> existingJobs) {
        JobExperience jobExperience = new JobExperience();
        jobExperience.setUser(user);

        // Gọi hàm lõi
        jobExperience.applyJobData(jobTitle, companyName, startDate, endDate, description, existingJobs);

        return jobExperience;
    }

    /**
     * Cập nhật JobExperience hiện tại
     */
    public void updateFromRequest(String jobTitle, String companyName,
                                  LocalDate startDate, LocalDate endDate,
                                  String description, List<JobExperience> otherJobs) {
        // Gọi hàm lõi (this = đối tượng hiện tại)
        this.applyJobData(jobTitle, companyName, startDate, endDate, description, otherJobs);
    }

    // =================================================================
    // VALIDATION LOGIC
    // =================================================================

    private void validateNoOverlap(LocalDate newStart, LocalDate newEnd, List<JobExperience> otherJobs) {
        LocalDate effectiveNewEnd = (newEnd == null) ? LocalDate.MAX : newEnd;

        for (JobExperience other : otherJobs) {
            // Bỏ qua chính nó khi Update
            if (this.id != null && this.id.equals(other.getId())) {
                continue;
            }

            LocalDate effectiveOtherEnd = (other.getEndDate() == null) ? LocalDate.MAX : other.getEndDate();

            // Logic overlap: (Start A <= End B) VÀ (End A >= Start B)
            boolean isOverlapping = !newStart.isAfter(effectiveOtherEnd) &&
                    !effectiveNewEnd.isBefore(other.getStartDate());

            if (isOverlapping) {
                String otherEndDisplay = (other.getEndDate() == null) ? "Present" : other.getEndDate().toString();
                throw new JobExperienceOverlapException(
                        String.format("Job experience period overlaps with another entry: %s at %s (%s - %s)",
                                other.getJobTitle(),
                                other.getCompanyName(),
                                other.getStartDate(),
                                otherEndDisplay)
                );
            }
        }
    }

    private void validateStartDateAfterBirth(LocalDate startDate) {
        if (this.user == null ||
                this.user.getPersonalInfo() == null ||
                this.user.getPersonalInfo().getDateOfBirth() == null) {
            return;
        }

        LocalDate dateOfBirth = this.user.getPersonalInfo().getDateOfBirth();

        // Quy định: Phải ít nhất 14 tuổi mới được đi làm (tuân theo luật lao động nhiều quốc gia)
        int MIN_AGE_TO_WORK = 14;
        LocalDate minWorkDate = dateOfBirth.plusYears(MIN_AGE_TO_WORK);

        if (startDate != null && startDate.isBefore(minWorkDate)) {
            throw new InvalidJobDateException(
                    String.format(
                            "Invalid job start date. User was born on %s and must be at least %d years old to work. Earliest valid date is %s.",
                            dateOfBirth,
                            MIN_AGE_TO_WORK,
                            minWorkDate
                    )
            );
        }
    }

    // =================================================================
    // UTILS
    // =================================================================

    public boolean isCurrentJob() {
        if (this.endDate == null) {
            return true; // Đang làm việc
        }
        return this.endDate.isAfter(LocalDate.now());
    }

    public String getDurationText() {
        String start = this.startDate != null ? this.startDate.toString() : "Unknown";
        String end = this.endDate != null ? this.endDate.toString() : "Present";
        return start + " - " + end;
    }
}
