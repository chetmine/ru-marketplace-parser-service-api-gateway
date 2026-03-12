package chetmine.marketplace.parser.controller.auth.register;

public record VerifyEmailCodeResponse(
        String email,
        String proofToken,
        String message
) {

}
