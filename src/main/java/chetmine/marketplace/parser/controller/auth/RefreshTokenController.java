package chetmine.marketplace.parser.controller.auth;

import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.JwtService;
import chetmine.marketplace.parser.service.RefreshTokenService;
import chetmine.marketplace.parser.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        try {
            Claims claims = jwtService.validateRefreshToken(refreshToken);
            Long userId = jwtService.extractUserId(claims);

            User user = userService.findById(userId);
            AuthTokensDTO tokens = refreshTokenService.refresh(refreshToken, user);

            ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                    .httpOnly(false)
                    .secure(false)
                    .path("/auth/refresh")
                    .maxAge(Duration.ofDays(30))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                    .body(new RefreshTokenResponse(tokens.accessToken(), "Refreshed successfully."));
        } catch(Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
