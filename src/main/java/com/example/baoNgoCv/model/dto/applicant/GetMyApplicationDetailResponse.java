package com.example.baoNgoCv.model.dto.applicant;

import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.ApplicationStatusHistory;
import com.example.baoNgoCv.model.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional; // <-- Cần import java.util.Optional

/**
 * DTO Chi tiết hồ sơ ứng tuyển của Jobseeker.
 */
public record GetMyApplicationDetailResponse(
        // ID của hồ sơ ứng tuyển
        Long applicationId,

        // Thông tin công việc
        String jobTitle,

        // Thông tin tên công ty
        String jobCompanyName,

        // Trạng thái hiện tại (PENDING, SHORTLISTED, INTERVIEW_SCHEDULED)
        ApplicationStatus currentStatus,

        // Ngày ứng viên nộp hồ sơ
        LocalDateTime applicationDate,

        // Thư xin việc (Nội dung ứng viên đã nộp)
        String coverLetter,

        // URL/Path đến file Resume (cho phép ứng viên tải lại file của mình)
        String resumeUrl,

        // Lịch sử trạng thái (Chỉ các bước quan trọng, không bao gồm Internal Notes)
        List<ApplicationLogDTO> publicLogs,

        // ===============================================
        //           THÔNG TIN LỊCH HẸN (MỚI)
        // ===============================================

        /**
         * Chứa lịch hẹn nếu currentStatus là INTERVIEW_SCHEDULED.
         * Dùng Optional để handle trường hợp không có lịch hẹn (NULL).
         */
        Optional<InterviewScheduleDTO> interviewSchedule
) {
    // DTO cho từng dòng log trong lịch sử
    public record ApplicationLogDTO(
            String statusTitle,
            LocalDateTime timestamp
    ) {
    }

    /**
     * DTO Chứa thông tin lịch phỏng vấn cụ thể (Ngày, Giờ, Địa điểm)
     */
    public record InterviewScheduleDTO(
            LocalDateTime interviewDateTime,
            String interviewType,
            String locationDetail
    ) {
    }

    // ========================================================================
    //  STATIC FACTORY METHOD: Chuyển đổi từ Entity -> DTO ngay tại đây
    // ========================================================================
    public static GetMyApplicationDetailResponse fromEntity(Applicant applicant) {

        // 1. Mapping Logs (Sort ngược chiều thời gian)
        List<ApplicationLogDTO> logs = applicant.getStatusHistory().stream()
                .sorted(Comparator.comparing(ApplicationStatusHistory::getStatusDate).reversed())
                .map(h -> new ApplicationLogDTO(
                        h.getStatus().name(), // Enum name (VD: PENDING)
                        h.getStatusDate()     // Sửa timestamp -> statusDate
                )).distinct()
                .toList();

        // 2. Mapping Interview (Null-safe) - Đã sửa theo Entity chuẩn
        Optional<InterviewScheduleDTO> interviewOpt = Optional.ofNullable(applicant.getInterviewSchedule())
                .map(i -> new InterviewScheduleDTO(
                        i.getInterviewDateTime(),
                        i.getInterviewType(),
                        i.getLocationDetail()
                ));


        // 3. Trả về DTO
        return new GetMyApplicationDetailResponse(
                applicant.getId(),
                applicant.getJobPosting().getTitle(),
                applicant.getJobPosting().getCompany().getName(),
                applicant.getCurrentStatusHistory().getStatus(),
                applicant.getCreatedAt(),
                applicant.getCoverLetter(),
                applicant.getResume(),
                logs,
                interviewOpt
        );
    }
}