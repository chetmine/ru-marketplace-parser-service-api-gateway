package chetmine.marketplace.parser.controller.auth.register;

import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.dto.CreateUserDTO;
import chetmine.marketplace.parser.dto.ResendCodeRequest;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.entity.UserStatus;
import chetmine.marketplace.parser.model.*;
import chetmine.marketplace.parser.service.*;
import chetmine.marketplace.parser.service.email.EmailVerificationService;
import chetmine.marketplace.parser.service.email.CodeResendRateLimiterService;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final CodeResendRateLimiterService codeResendRateLimiterService;
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    private final ProofTokenService proofTokenService;
    private final AuthService authService;
    private final PasswordService passwordService;

    @PostMapping("/send-code")
    public ResponseEntity<?> registerEmail(
            @RequestBody SendCodeRequest req
    ) {
        try {
            userService.create(new CreateUserDTO(
                    req.email(), passwordService.hash(req.password())
            ));

            codeResendRateLimiterService.register(req.email());
            String token = emailVerificationService.sendCode(req.email());

            return ResponseEntity.ok().body(new SendCodeResponse(token));
        } catch (EntityExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(
            @RequestBody ResendCodeRequest req
    ) {
        try {
            codeResendRateLimiterService.register(req.email());
            String token = emailVerificationService.sendCode(req.email());

            return ResponseEntity.ok().body(new SendCodeResponse(token));
        } catch (TooManyResendCodeRequestsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyEmailCode(
            @RequestBody VerifyEmailCodeRequest req
    ) {

        try {
            String email = emailVerificationService.verifyCode(req.riskToken(), req.code());
            User user = userService.findByEmail(email);

            if (user.getStatus() == UserStatus.VERIFIED) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already verified");
            }

            userService.verify(user.getId());

            String token = proofTokenService.issue(user.getEmail(), ProofTokenAction.REGISTER);

            return ResponseEntity.ok().body(new VerifyEmailCodeResponse(email, token, "Email verified successfully."));
        } catch (RiskTokenException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest req
    ) {
        try {
            String email = proofTokenService.consume(req.proofToken(), ProofTokenAction.REGISTER);
            User user = userService.findByEmail(email);

            AuthTokensDTO tokens = authService.register(user);

            ResponseCookie resCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                    .httpOnly(false)
                    .secure(false)
                    .path("/auth/refresh")
                    .maxAge(30 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                    .body(new RegistrationResponse(tokens.accessToken()))
                    ;
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

