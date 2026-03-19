package chetmine.marketplace.parser.scheduled;

import chetmine.marketplace.parser.entity.UserStatus;
import chetmine.marketplace.parser.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingUserCleanupScheduler {
    private final UserRepo userRepository;

    @Value("${app.cleanup.pending-users.expiry-minutes:30}")
    private int expiryMinutes;

    @Scheduled(fixedRateString = "${app.cleanup.pending-users.interval-ms:300000}")
    @Transactional
    public void cleanupPendingUsers() {
        LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(expiryMinutes);

        int deleted = userRepository.deleteByStatusAndCreatedAtBefore(
                UserStatus.PENDING_VERIFICATION,
                expiryThreshold
        );

        if (deleted > 0) {
            log.info("Cleaned up {} pending user(s) older than {} minutes", deleted, expiryMinutes);
        }
    }
}
