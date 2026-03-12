package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.model.ProofTokenAction;
import chetmine.marketplace.parser.model.ProofTokenData;
import chetmine.marketplace.parser.model.ProofTokenException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class ProofTokenService {
    private static final String KEY = "proof:token:";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Qualifier("proofTokenTemplate")
    private final RedisTemplate<String, ProofTokenData> redisTemplate;

    public ProofTokenService(RedisTemplate<String, ProofTokenData> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String issue(String subject, ProofTokenAction action) {
        String uuid = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                KEY + uuid,
                new ProofTokenData(subject, action),
                TTL
        );

        return uuid;
    }

    public String consume(String token, ProofTokenAction action) {
        ProofTokenData tokenData = redisTemplate.opsForValue().getAndDelete(KEY + token);

        if (tokenData == null) {
            throw new ProofTokenException("Proof token expired or not found.");
        }

        if (tokenData.action() != action) {
            throw new ProofTokenException("Proof token is invalid.");
        }

        return tokenData.subject();
    }
}
