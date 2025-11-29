package com.example.baoNgoCv.model.dto.user;

public record PostApplyJobResponse(
        Long applicantId,
        String redirectUrl,
        String status,
        String toastType
) {

    public static PostApplyJobResponse success(Long applicantId, String redirectUrl) {
        String highlightUrl = redirectUrl.contains("?")
                ? redirectUrl + "&highlight=" + applicantId
                : redirectUrl + "?highlight=" + applicantId;

        return new PostApplyJobResponse(
                applicantId,
                highlightUrl,  // ✅ URL đã có ?highlight=applicantId
                "success",
                "success"
        );
    }
}
