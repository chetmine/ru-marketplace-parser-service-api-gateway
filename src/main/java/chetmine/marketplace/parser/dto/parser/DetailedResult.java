package chetmine.marketplace.parser.dto.parser;

import java.util.List;

public record DetailedResult(
        Product product,
        boolean isDone,
        String error
) {
}
