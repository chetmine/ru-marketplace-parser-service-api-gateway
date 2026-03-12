package chetmine.marketplace.parser.dto.parser;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TaskParams {
    private Boolean retryOnParserExposed;
}
