package chetmine.marketplace.parser.service.email;

import chetmine.marketplace.parser.service.TooManyResendCodeRequestsException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CodeResendRateLimiterService {

    private static final String KEY_ATTEMPTS = "email:resend:attempts:";
    private static final String KEY_COOLDOWNS= "email:resend:cooldown:";

    private static final List<Duration> COOLDOWNS = List.of(
            Duration.ofMinutes(1),
            Duration.ofMinutes(2),
            Duration.ofMinutes(5),
            Duration.ofMinutes(15)
    );
    private static final int MAX_ATTEMPTS = 5;

    private final RedisTemplate<String, String> emailCodeResendRateLimiterTemplate;

    public void register(String email) {
        String cooldownTtl = emailCodeResendRateLimiterTemplate.opsForValue().get(KEY_COOLDOWNS + email);
        if (cooldownTtl != null) {
            long secondsLeft = emailCodeResendRateLimiterTemplate.getExpire(KEY_COOLDOWNS + email, TimeUnit.SECONDS);
            throw new TooManyResendCodeRequestsException(
                    "Please wait %d seconds before requesting a new code".formatted(secondsLeft)
            );
        }

        String attemptsStr = emailCodeResendRateLimiterTemplate.opsForValue().get(KEY_ATTEMPTS + email);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= MAX_ATTEMPTS) {
            long secondsLeft = emailCodeResendRateLimiterTemplate.getExpire(KEY_ATTEMPTS + email, TimeUnit.SECONDS);
            throw new TooManyResendCodeRequestsException(
                    "Daily limit reached. Try again in %d seconds".formatted(secondsLeft)
            );
        }

        registerAttempt(email, attempts);
    }

    private void registerAttempt(String email, int attempt) {
        int newAttempts = attempt + 1;

        emailCodeResendRateLimiterTemplate.opsForValue().set(
                KEY_ATTEMPTS + email,
                String.valueOf(newAttempts),
                secondsUntilMidnight(),
                TimeUnit.SECONDS
        );

        if (newAttempts < MAX_ATTEMPTS) {
            Duration cooldown = COOLDOWNS.get(Math.min(newAttempts - 1, COOLDOWNS.size() - 1));
            emailCodeResendRateLimiterTemplate.opsForValue().set(
                    KEY_COOLDOWNS + email,
                    "1",
                    cooldown.getSeconds(),
                    TimeUnit.SECONDS
            );
        }
    }

    private long secondsUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return Duration.between(now, midnight).getSeconds();
    }
}
