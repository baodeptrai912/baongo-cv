package com.example.baoNgoCv.model.valueObject;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDetails {

    @Enumerated(EnumType.STRING)
    private AccountTier accountTier;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    private LocalDateTime planExpirationDate;

    private Integer jobPostingsThisCycle;

    private LocalDateTime cycleStartDate;

    public static SubscriptionDetails createFreePlan() {
        return SubscriptionDetails.builder()
                .accountTier(AccountTier.FREE)
                .billingCycle(BillingCycle.MONTHLY)
                .planExpirationDate(null)
                .jobPostingsThisCycle(0)
                .cycleStartDate(LocalDateTime.now())
                .build();
    }

    public boolean isExpired() {
        if (this.planExpirationDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.planExpirationDate);
    }

    public static SubscriptionDetails upgradeTo(AccountTier targetTier, BillingCycle billingCycle) {
        long monthsToAdd = (billingCycle == BillingCycle.YEARLY) ? 12 : 1;
        return SubscriptionDetails.builder()
                .accountTier(targetTier)
                .billingCycle(billingCycle)
                .planExpirationDate(LocalDateTime.now().plusMonths(monthsToAdd))
                .cycleStartDate(LocalDateTime.now())
                .jobPostingsThisCycle(0)
                .build();
    }

    /**
     * Kiểm tra trạng thái gói cước. Nếu đã hết hạn, tự động hạ cấp về gói FREE.
     * @return true nếu có sự thay đổi (hạ cấp), ngược lại là false.
     */
    public boolean validateAndRefreshState() {
        if (this.accountTier != AccountTier.FREE && isExpired()) {
            this.accountTier = AccountTier.FREE;
            this.billingCycle = BillingCycle.MONTHLY;
            this.planExpirationDate = null;
            resetCycle();
            return true;
        }
        return false;
    }

    public int getCurrentJobPostingsThisCycle() {
        if (isCycleOver()) {
            resetCycle();
        }
        return this.jobPostingsThisCycle;
    }

    public boolean canPostNewJob() {

        return this.getCurrentJobPostingsThisCycle() < this.accountTier.getMaxJobPostings();
    }

    public void incrementJobPostingsThisCycle() {
        this.jobPostingsThisCycle++;
    }

    private boolean isCycleOver() {
        if (this.cycleStartDate == null) {
            return false;
        }
        LocalDateTime nextMonthlyResetDate = this.cycleStartDate.plusMonths(1);
        return LocalDateTime.now().isAfter(nextMonthlyResetDate);
    }

    private void resetCycle() {
        this.cycleStartDate = LocalDateTime.now();
        this.jobPostingsThisCycle = 0;
    }
}
