package chetmine.marketplace.parser.model;

public record RiskTokenData(
        String email,
        String codeHash,
        int attempts
) {}
