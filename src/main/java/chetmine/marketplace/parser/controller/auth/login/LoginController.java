package chetmine.marketplace.parser.controller.auth.login;


import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.model.AuthException;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.AuthService;
import chetmine.marketplace.parser.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest req
    ) {
        try {

            User user = userService.findByEmail(req.email());
            AuthTokensDTO tokens = authService.login(user, req.password());

            ResponseCookie cookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                    .httpOnly(false)
                    .secure(false)
                    .path("/auth/refresh")
                    .maxAge(Duration.ofDays(30))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(new LoginResponse(tokens.accessToken(), "Welcome, " + req.email()));
        } catch (EntityNotFoundException | AuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
