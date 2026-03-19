package chetmine.marketplace.parser.controller.auth.login;


import chetmine.marketplace.parser.controller.auth.register.SendCodeResponse;
import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.exception.AuthException;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.AuthService;
import chetmine.marketplace.parser.service.UserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@Tag(name = "authentication")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final UserService userService;

    @Operation(
            summary = "Вход в аккаунт",
            description = "Аутентифицирует пользователя и возращает access токен и 'refreshToken' в куках."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    })

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req
    ) {
        User user = userService.findByEmail(req.email());
        AuthTokensDTO tokens = authService.login(user, req.password());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(30))
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new LoginResponse(tokens.accessToken(), "Welcome, " + req.email()));
    }
}
