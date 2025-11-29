package com.example.baoNgoCv.event.applicant;

import com.example.baoNgoCv.model.entity.Applicant;

/**
 * Một record dữ liệu đơn giản, bất biến, đại diện cho sự kiện một ứng viên rút đơn ứng tuyển.
 * Đây là một record thuần túy, không bị ràng buộc vào ApplicationEvent của Spring, giúp nó có thể tái sử dụng tốt hơn.
 * Cơ chế sự kiện của Spring sẽ tự động bọc nó trong một PayloadApplicationEvent khi được phát đi.
 *
 * @param applicant Entity ứng viên đã rút đơn.
 */
public record ApplicantWithdrewEvent(Applicant applicant) {

    /**
     * Factory method to create a new ApplicantWithdrewEvent.
     * @param applicant The applicant entity that has been withdrawn.
     * @return A new instance of ApplicantWithdrewEvent.
     */
    public static ApplicantWithdrewEvent from(Applicant applicant) {
        return new ApplicantWithdrewEvent(applicant);
    }
}