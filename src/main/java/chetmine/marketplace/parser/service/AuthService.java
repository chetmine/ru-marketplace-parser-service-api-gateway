package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.exception.AuthException;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordService passwordService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    public AuthTokensDTO register(User user) {
        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            throw new AuthException("User must be verified.");
        }

        userService.register(user.getId());
        subscriptionService.createDefault(user.getId());

        AuthTokensDTO tokens = jwtService.issueTokens(user);
        refreshTokenService.save(user.getId(), tokens.refreshToken(), Duration.ofDays(30).getSeconds());

        return tokens;
    }

    public AuthTokensDTO login(User user, String rawPassword) {
        if (!user.isRegistered()) {
            throw new AuthException("User must be registered.");
        }

        if (!passwordService.verify(rawPassword, user.getPasswordHash())) {
            throw new AuthException("Invalid credentials.");
        }

        AuthTokensDTO tokens = jwtService.issueTokens(user);
        refreshTokenService.save(user.getId(), tokens.refreshToken(), Duration.ofDays(30).getSeconds());

        return tokens;
    }
}
