package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.dto.SubscriptionStatusDto;
import chetmine.marketplace.parser.entity.Subscription;
import chetmine.marketplace.parser.entity.SubscriptionPlan;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.model.RequestLimitExceededException;
import chetmine.marketplace.parser.repo.SubscriptionRepo;
import chetmine.marketplace.parser.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final UserRepo userRepo;
    private final SubscriptionRepo subscriptionRepo;

    private final int defaultLimit = 20;


    @Transactional
    public void createDefault(Long userId) {
        User user = userRepo.getReferenceById(userId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(SubscriptionPlan.FREE)
                .requestsLimit(defaultLimit)
                .requestsUsed(0)
                .resetAt(Instant.now().plusSeconds(30 * 24 * 60 * 60))
                .build();

        subscriptionRepo.save(subscription);
    }

    @Transactional
    public void consume(Long userId) {
        Subscription subscription = subscriptionRepo.getReferenceById(userId);
        resetIfExpired(subscription);

        if (subscription.getRequestsUsed() >= subscription.getRequestsLimit()) {
            throw new RequestLimitExceededException("Limit reached.");
        }

        subscription.setRequestsUsed(subscription.getRequestsUsed() + 1);
        subscriptionRepo.save(subscription);
    }

    @Transactional
    public SubscriptionStatusDto getStatus(Long userId) {
        Subscription subscription = getSubscription(userId);
        resetIfExpired(subscription);

        int used = subscription.getRequestsUsed();

        return SubscriptionStatusDto.builder()
                .requestsUsed(used)
                .requestsLimit(subscription.getRequestsLimit())
                .requestsRemaining(Math.max(0, subscription.getRequestsLimit() - used))
                .resetAt(LocalDateTime.ofInstant(subscription.getResetAt(), ZoneId.systemDefault()))
                .build();
    }

    @Transactional
    protected void resetIfExpired(Subscription subscription) {
        if (LocalDateTime.now().isAfter(LocalDateTime.ofInstant(subscription.getResetAt(), ZoneId.systemDefault()))) {
            subscription.setRequestsUsed(0);
            subscription.setResetAt(Instant.now().plusSeconds(30 * 24 * 60 * 60));
            subscriptionRepo.save(subscription);
        }
    }

    private Subscription getSubscription(Long userId) {
        return subscriptionRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Subscription not found for userId=" + userId));
    }
}
