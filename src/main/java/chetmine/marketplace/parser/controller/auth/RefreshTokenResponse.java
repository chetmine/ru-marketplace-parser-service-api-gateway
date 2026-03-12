package chetmine.marketplace.parser.controller.auth;

public record RefreshTokenResponse(
        String accessToken,
        String message
) {
}
