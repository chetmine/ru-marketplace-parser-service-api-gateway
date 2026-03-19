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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "registration")
@RestController
@RequestMapping("/api/auth/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final CodeResendRateLimiterService codeResendRateLimiterService;
    private final EmailVerificationService emailVerificationService;
    private final UserService userService;
    private final ProofTokenService proofTokenService;
    private final AuthService authService;
    private final PasswordService passwordService;

    @Operation(
            summary = "Отправка кода для регистрации",
            description = "Отправляет email код для регистрации пользователя, если он еще не зарегистрирован."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код отправлен успешно",
                content = @Content(schema = @Schema(implementation = SendCodeResponse.class)))
    })

    @PostMapping("/send-code")
    public ResponseEntity<?> registerEmail(
            @RequestBody SendCodeRequest req
    ) throws MessagingException {
        userService.create(new CreateUserDTO(
                req.email(), passwordService.hash(req.password())
        ));

        codeResendRateLimiterService.register(req.email());
        String token = emailVerificationService.sendCode(req.email());

        return ResponseEntity.ok().body(new SendCodeResponse(token));
    }

    @Operation(
            summary = "Переотправка кода для регистрации",
            description = "Отправляет следующий код для регистрации с новым riskToken."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Код отправлен успешно",
                    content = @Content(schema = @Schema(implementation = SendCodeResponse.class)))
    })

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(
            @RequestBody ResendCodeRequest req
    ) throws MessagingException {
        codeResendRateLimiterService.register(req.email());
        String token = emailVerificationService.sendCode(req.email());

        return ResponseEntity.ok().body(new SendCodeResponse(token));
    }

    @Operation(
            summary = "Подтверждение кода для регистрации",
            description = "Проверяет код, отправленный на email пользователя. Возвращает proofToken, который пригодится для конечной регистрации пользователя"
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Подтверждено.",
                    content = @Content(schema = @Schema(implementation = VerifyEmailCodeResponse.class)))
    })

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyEmailCode(
            @RequestBody VerifyEmailCodeRequest req
    ) {
        String email = emailVerificationService.verifyCode(req.riskToken(), req.code());
        User user = userService.findByEmail(email);

        if (user.getStatus() == UserStatus.VERIFIED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already verified");
        }

        userService.verify(user.getId());

        String token = proofTokenService.issue(user.getEmail(), ProofTokenAction.REGISTER);

        return ResponseEntity.ok().body(new VerifyEmailCodeResponse(email, token, "Email verified successfully."));
    }


    @Operation(
            summary = "Конечная регистрация пользователя",
            description = "Региструет пользователя, используя proofToken из предыдущего запроса. Возращает куку 'refreshToken'."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная регистрация.",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class)))
    })

    @PostMapping("/complete")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest req
    ) {
        String email = proofTokenService.consume(req.proofToken(), ProofTokenAction.REGISTER);
        User user = userService.findByEmail(email);

        AuthTokensDTO tokens = authService.register(user);

        ResponseCookie resCookie = ResponseCookie.from("refreshToken", tokens.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(30 * 24 * 60 * 60)
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(new RegistrationResponse(tokens.accessToken()))
                ;
    }
}

