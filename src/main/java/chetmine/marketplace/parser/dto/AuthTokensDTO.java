package chetmine.marketplace.parser.dto;

public record AuthTokensDTO(
        String accessToken,
        String refreshToken
) {}
