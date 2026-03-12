package chetmine.marketplace.parser.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
public record SubscriptionStatusDto(
        int requestsUsed,
        int requestsLimit,
        int requestsRemaining,
        LocalDateTime resetAt
) {
}
