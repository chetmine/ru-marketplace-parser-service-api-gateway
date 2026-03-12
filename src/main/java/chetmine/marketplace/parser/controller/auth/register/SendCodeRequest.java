package chetmine.marketplace.parser.controller.auth.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendCodeRequest(
        @NotBlank(message = "email is required")
        @Email(message = "Valid email is required")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 20, message = "password must be between 8 and 20 symbols")
        String password
) { }
