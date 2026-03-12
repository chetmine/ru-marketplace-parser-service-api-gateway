package chetmine.marketplace.parser.dto;

public record CreateUserDTO(
        String email,
        String passwordHash
) {
}
