package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import com.example.baoNgoCv.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentLinkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Company company;

    @Column(nullable = false, unique = true)
    private Long orderCode;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountTier targetTier;

    @Lob
    @Column(name = "qr_code")
    private String qrCode;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public static Payment create(String paymentLinkId, Company company, Long orderCode, Integer amount, BillingCycle billingCycle, AccountTier targetTier, String qrCode) {
        return Payment.builder()
                .paymentLinkId(paymentLinkId)
                .company(company)
                .orderCode(orderCode)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .billingCycle(billingCycle)
                .targetTier(targetTier)
                .qrCode(qrCode)
                .build();
    }

    public void updateStatusSuccess() {
        this.status = PaymentStatus.SUCCESSFUL;
    }

    public void updateStatusFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isSuccessful() {
        return PaymentStatus.SUCCESSFUL.equals(this.status);
    }
}
