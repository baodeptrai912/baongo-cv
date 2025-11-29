package com.example.baoNgoCv.model.dto.payment;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import com.example.baoNgoCv.model.enums.PaymentStatus;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record GetTransactionHistoryResponse(
        Page<TransactionHistoryProjection> transactions,
        long successfulCount,
        long pendingCount,
        long failedCount
) {

    /**
     * Projection interface để chỉ lấy các trường cần thiết từ Entity Payment.
     * Giúp tối ưu hóa câu lệnh SQL, không lấy các trường không cần thiết.
     */
    public interface TransactionHistoryProjection {
        Long getOrderCode();
        Integer getAmount();
        AccountTier getTargetTier();
        BillingCycle getBillingCycle();
        PaymentStatus getStatus();
        LocalDateTime getCreatedAt();
        String getQrCode();
    }
}
