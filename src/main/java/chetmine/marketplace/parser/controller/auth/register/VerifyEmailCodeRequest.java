package chetmine.marketplace.parser.controller.auth.register;

public record VerifyEmailCodeRequest(
        String riskToken,
        String code
) {
}
