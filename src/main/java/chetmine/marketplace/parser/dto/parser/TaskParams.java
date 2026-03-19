package chetmine.marketplace.parser.dto.parser;

import lombok.*;

import java.util.List;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskParams {
    private Boolean retryOnParserExposed;
    private String marketplace;
}
