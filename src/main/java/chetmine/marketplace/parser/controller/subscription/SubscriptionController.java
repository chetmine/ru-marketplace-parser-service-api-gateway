package chetmine.marketplace.parser.controller.subscription;


import chetmine.marketplace.parser.dto.SubscriptionStatusDto;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusDto> getStatus(
            Authentication authentication) {
        try {
            UsernamePasswordAuthenticationToken auth =
                    (UsernamePasswordAuthenticationToken) authentication;

            User user = (User) auth.getPrincipal();

            return ResponseEntity.ok(subscriptionService.getStatus(user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
