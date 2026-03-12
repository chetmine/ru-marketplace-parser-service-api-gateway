package chetmine.marketplace.parser.service.risk.token;

import chetmine.marketplace.parser.model.RiskTokenData;
import chetmine.marketplace.parser.model.RiskTokenException;
import chetmine.marketplace.parser.service.RiskTokenService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class EmailCodeRiskTokenService extends RiskTokenService<RiskTokenData> {
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 3;

    public EmailCodeRiskTokenService(@Qualifier("emailCodeRiskToken") RedisTemplate<String, RiskTokenData> redisTemplate) {
        super(redisTemplate, "email", TTL, MAX_ATTEMPTS);
    }

    @Override
    protected RiskTokenData buildData(String subject, String code, int attempts) {
        return new RiskTokenData(subject, code, attempts);
    }

    @Override
    protected void validateCode(RiskTokenData data, String code) {
        if (!data.codeHash().equals(code)) {
            throw new RiskTokenException("Invalid code");
        }
    }

    @Override
    protected String extractSubject(RiskTokenData data) {
        return data.email();
    }

    @Override
    protected int extractAttempts(RiskTokenData data) {
        return data.attempts();
    }

    @Override
    protected String extractCodeHash(RiskTokenData data) {
        return data.codeHash();
    }
}
