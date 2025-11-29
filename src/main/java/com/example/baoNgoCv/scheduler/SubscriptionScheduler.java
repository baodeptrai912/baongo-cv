package com.example.baoNgoCv.scheduler;

import com.example.baoNgoCv.service.domainService.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final CompanyService companyService;

    /**
     * Tác vụ chạy tự động vào 00:01 mỗi ngày để dọn dẹp các tài khoản hết hạn.
     */
    @Scheduled(cron = "0 1 0 * * ?") // Chạy vào 00:01:00 hàng ngày
    public void downgradeExpiredSubscriptions() {
        log.info("[SCHEDULER] Starting scheduled job: Downgrade Expired Subscriptions.");
        try {
            companyService.downgradeExpiredAccounts();
        } catch (Exception e) {
            log.error("[SCHEDULER] Error during scheduled downgrade of expired subscriptions.", e);
        }
        log.info("[SCHEDULER] Finished scheduled job: Downgrade Expired Subscriptions.");
    }
}
