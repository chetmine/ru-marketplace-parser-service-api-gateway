package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.model.AuthException;
import chetmine.marketplace.parser.entity.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY = "refresh:token:";

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtService jwtService;

    public void save(Long userId, String token, long expirySeconds) {
        String tokenHash = hash(token);
        stringRedisTemplate.opsForValue().set(KEY + userId, tokenHash, Duration.ofSeconds(expirySeconds));
    }

    public AuthTokensDTO refresh(String refreshToken, User user) {
        Claims claims = jwtService.validateRefreshToken(refreshToken);
        Long userId = jwtService.extractUserId(claims);

        String storedHash = stringRedisTemplate.opsForValue().get(KEY + userId);
        String currentHash = hash(refreshToken);

        if (storedHash == null || !storedHash.equals(currentHash)) {
            invalidate(userId);
            throw new AuthException("Unauthorized refresh token access.");
        }

        AuthTokensDTO tokens = jwtService.issueTokens(user);

        save(userId, tokens.refreshToken(), Duration.ofDays(30).getSeconds());

        return tokens;
    }

    public void invalidate(Long userId) {
        stringRedisTemplate.delete(KEY + userId);
    }

    private String hash(String token) {
        return DigestUtils.md5DigestAsHex(token.getBytes());
    }
}
