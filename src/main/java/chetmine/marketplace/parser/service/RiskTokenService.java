package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.model.RiskTokenException;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.UUID;

public abstract class RiskTokenService<T> {
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final RedisTemplate<String, T> redisTemplate;
    private final String keyPrefix;
    private final Duration ttl;
    private final int maxAttempts;

    protected RiskTokenService(
            RedisTemplate<String, T> redisTemplate,
            String keyPrefix,
            Duration ttl,
            int maxAttempts
    ) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = "risk:token:" + keyPrefix + ":";
        this.ttl = ttl;
        this.maxAttempts = maxAttempts;
    }

    protected RiskTokenService(RedisTemplate<String, T> redisTemplate, String keyPrefix) {
        this(redisTemplate, keyPrefix, DEFAULT_TTL, DEFAULT_MAX_ATTEMPTS);
    }

    protected abstract T buildData(String subject, String code, int attempts);

    protected abstract void validateCode(T data, String code);

    protected abstract String extractSubject(T data);

    protected abstract int extractAttempts(T data);

    protected abstract String extractCodeHash(T data);

    public String issue(String subject, String code) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                keyPrefix + token,
                buildData(subject, code, 0),
                ttl
        );
        return token;
    }

    public String verify(String riskToken, String code) {
        T data = getData(riskToken);

        if (extractAttempts(data) >= maxAttempts) {
            throw new RiskTokenException("Too many attempts");
        }

        try {
            validateCode(data, code);
        } catch (RiskTokenException e) {
            redisTemplate.opsForValue().set(
                    keyPrefix + riskToken,
                    buildData(extractSubject(data), extractCodeHash(data), extractAttempts(data) + 1),
                    ttl
            );
            throw e;
        }

        revoke(riskToken);
        return extractSubject(data);
    }

    public void revoke(String riskToken) {
        redisTemplate.delete(keyPrefix + riskToken);
    }

    private @NonNull T getData(String key) {
        T data = redisTemplate.opsForValue().get(keyPrefix + key);
        if (data == null) {
            throw new RiskTokenException("Risk token expired or not found.");
        }
        return data;
    }

}
