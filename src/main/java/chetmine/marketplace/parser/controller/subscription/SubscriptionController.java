package chetmine.marketplace.parser.controller.subscription;


import chetmine.marketplace.parser.controller.auth.RefreshTokenResponse;
import chetmine.marketplace.parser.dto.SubscriptionStatusDto;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "subscription")
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Refresh token",
            description = "Обновляет и возращает новый access токен."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно",
                    content = @Content(schema = @Schema(implementation = SubscriptionStatusDto.class)))
    })
    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusDto> getStatus(
            Authentication authentication) {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) authentication;

        User user = (User) auth.getPrincipal();

        return ResponseEntity.ok(subscriptionService.getStatus(user.getId()));
    }
}
