package chetmine.marketplace.parser.controller.auth.register;

import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.UUID;

public record VerifyEmailCodeResponse(
        @Email
        String email,
        @UUID
        String proofToken,
        String message
) {

}
