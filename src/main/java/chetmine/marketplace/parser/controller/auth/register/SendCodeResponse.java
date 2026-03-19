package chetmine.marketplace.parser.controller.auth.register;

import org.hibernate.validator.constraints.UUID;

public record SendCodeResponse(
        @UUID
        String riskToken
) {
}
