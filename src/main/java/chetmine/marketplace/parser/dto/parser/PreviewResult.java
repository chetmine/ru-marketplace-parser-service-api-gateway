package chetmine.marketplace.parser.dto.parser;

import java.util.List;

public record PreviewResult(
        List<ProductPreview> products,
        boolean isDone,
        String error
) {
}
