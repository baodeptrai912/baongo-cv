package com.example.baoNgoCv.event;

import com.example.baoNgoCv.event.user.UserAccountDeletedEvent;
import com.example.baoNgoCv.service.utilityService.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Slf4j
@Component
public class CleanerHandler {

    private final FileService fileService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserDeleted(UserAccountDeletedEvent event) {
        log.info("🧹 [CLEANER_HANDLER] Started - User account deletion cleanup event received for email: '{}'", event.email());
        long startTime = System.currentTimeMillis();

        try {
            if (event.filePaths() == null || event.filePaths().isEmpty()) {
                log.info("🧹 [CLEANER_HANDLER] No files to delete for user: '{}'. Task completed.", event.email());
                return;
            }

            log.info("🧹 [CLEANER_HANDLER] Found {} file(s) to delete for user: '{}'", event.filePaths().size(), event.email());

            int ok = 0, fail = 0;
            for (String path : event.filePaths()) {
                try {
                    log.debug("🧹 [CLEANER_HANDLER] Deleting file: {}", path);
                    fileService.deleteFile(path);
                    ok++;
                    log.debug("🧹 [CLEANER_HANDLER] ✓ Successfully deleted: {}", path);
                } catch (Exception ex) {
                    fail++;
                    log.error("🧹 [CLEANER_HANDLER] ❌ Failed to delete file '{}' for user '{}'", path, event.email(), ex);
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            if (fail > 0) {
                log.warn("🧹 [CLEANER_HANDLER] ⚠️  PARTIAL SUCCESS for user '{}' in {}ms. Deleted: {}, Failed: {}.",
                        event.email(), duration, ok, fail);
            } else {
                log.info("🧹 [CLEANER_HANDLER] ✅ SUCCESS: Cleanup completed for user '{}' in {}ms. All {} files deleted.",
                        event.email(), duration, ok);
            }

        } catch (Exception ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("🧹 [CLEANER_HANDLER] ❌ CRITICAL ERROR during cleanup for user '{}' after {}ms", event.email(), duration, ex);
        }
    }

}
