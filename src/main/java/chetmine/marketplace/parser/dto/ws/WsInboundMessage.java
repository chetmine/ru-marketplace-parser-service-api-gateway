package chetmine.marketplace.parser.dto.ws;

import chetmine.marketplace.parser.dto.parser.TaskParams;
import lombok.Data;

@Data
public class WsInboundMessage {
    private String type;
    private String query;
    private TaskParams params;
}
