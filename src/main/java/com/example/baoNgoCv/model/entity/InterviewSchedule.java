package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "interview_schedule")
public class InterviewSchedule {

    @Id
    private Long id;

    @MapsId // Lấy giá trị của khóa chính từ đối tượng được map (Applicant)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id") // Tên cột khóa ngoại (thường trùng tên ID, không cần unique=true vì nó đã là PK)
    private Applicant applicant; // Mối quan hệ này là nguồn cấp ID

    // Dữ liệu cụ thể về lịch hẹn
    private LocalDateTime interviewDateTime;

    @Column(length = 50)
    private String interviewType;

    @Column(length = 255)
    private String locationDetail;

    @Column(columnDefinition = "TEXT")
    private String fullEmailContent;

    // --- Phương thức tiện ích để tạo lịch hẹn mới ---
    public static InterviewSchedule createNew(Applicant applicant, LocalDateTime dateTime, String type, String location, String content) {
        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setApplicant(applicant);
        schedule.setInterviewDateTime(dateTime);
        schedule.setInterviewType(type);
        schedule.setLocationDetail(location);
        schedule.setFullEmailContent(content);
        return schedule;
    }
}