package com.example.baoNgoCv.event.jobposting;

import com.example.baoNgoCv.model.enums.ExpireReason;
import java.util.List;

/**
 * Event khi job posting hết hạn
 * ✅ Truyền payload cụ thể, không truyền entity objects
 * Handler sẽ nhận được tất cả dữ liệu cần thiết, không phải query DB
 */
public record JobPostingExpiredEvent(
        Long jobPostingId,
        String jobTitle,
        Long companyId,
        String employerEmail,
        String employerName,
        ExpireReason reason,
        List<Long> batchJobIds     // For batch expiry
) {

    // Constructor cho single job
    public JobPostingExpiredEvent(
            Long jobPostingId,
            String jobTitle,
            Long companyId,
            String employerEmail,
            String employerName,
            ExpireReason reason
    ) {
        this(jobPostingId, jobTitle, companyId, employerEmail, employerName, reason, null);
    }

    // Constructor cho batch jobs (tuỳ chọn)
    public JobPostingExpiredEvent(
            List<Long> batchJobIds,
            ExpireReason reason
    ) {
        this(null, null, null, null, null, reason, batchJobIds);
    }
}
