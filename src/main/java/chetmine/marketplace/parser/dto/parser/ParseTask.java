package chetmine.marketplace.parser.dto.parser;

import lombok.Builder;

@Builder
public record ParseTask(
        String sessionId,
        String type,
        String query,
        TaskParams params
) {
}
