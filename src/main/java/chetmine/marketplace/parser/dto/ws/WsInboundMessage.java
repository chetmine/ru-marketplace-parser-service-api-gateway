package chetmine.marketplace.parser.dto.ws;

import lombok.Data;

@Data
public class WsInboundMessage {
    private String type;
    private String query;
}
