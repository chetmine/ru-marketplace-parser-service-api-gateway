package chetmine.marketplace.parser.dto.ws;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WsOutboundMessage {
    private String type; // "preview_chunk" | "detailed_result" | "error"
    private Object data;

    private String code;
    private Object meta;

    public static WsOutboundMessage previewChunk(Object data) {
        return WsOutboundMessage.builder()
                .type("preview_chunk")
                .data(data)
                .build();
    }

    public static WsOutboundMessage detailedResult(Object data) {
        return WsOutboundMessage.builder()
                .type("detailed_result")
                .data(data)
                .build();
    }

    public static WsOutboundMessage error(String code, Object meta) {
        return WsOutboundMessage.builder()
                .type("error")
                .code(code)
                .meta(meta)
                .build();
    }

    public static WsOutboundMessage finish(Object meta) {
        return WsOutboundMessage.builder()
                .type("finish")
                .meta(meta)
                .build();
    }
}
