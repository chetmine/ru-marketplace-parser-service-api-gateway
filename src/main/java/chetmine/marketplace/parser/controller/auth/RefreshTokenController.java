package chetmine.marketplace.parser.controller.auth;

import chetmine.marketplace.parser.controller.auth.login.LoginResponse;
import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.JwtService;
import chetmine.marketplace.parser.service.RefreshTokenService;
import chetmine.marketplace.parser.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Tag(name = "authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final JwtService jwtService;

    @Operation(
            summary = "Refresh token",
            description = "Обновляет и возращает новый access токен."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = RefreshTokenResponse.class)))
    })

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken") String refreshToken
    ) {
        Claims claims = jwtService.validateRefreshToken(refreshToken);
        Long userId = jwtService.extractUserId(claims);

        User user = userService.findById(userId);
        AuthTokensDTO tokens = refreshTokenService.refresh(refreshToken, user);

        ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(30))
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(new RefreshTokenResponse(tokens.accessToken(), "Refreshed successfully."));
    }
}
