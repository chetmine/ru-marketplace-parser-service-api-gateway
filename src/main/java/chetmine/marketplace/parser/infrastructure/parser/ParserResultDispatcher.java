package chetmine.marketplace.parser.infrastructure.parser;


import chetmine.marketplace.parser.dto.ws.WsOutboundMessage;
import chetmine.marketplace.parser.dto.parser.DetailedResult;
import chetmine.marketplace.parser.dto.parser.PreviewResult;
import chetmine.marketplace.parser.infrastructure.ws.WsSessionRegistry;
import chetmine.marketplace.parser.model.WsSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParserResultDispatcher {

    private final WsSessionRegistry wsSessionRegistry;
    private final ObjectMapper objectMapper;


    /**
     *
     * @return Returns true on parser finished.
     */

    public boolean dispatch(String sessionId, String taskType, Message message) throws Exception {
        WsSession wsSession = wsSessionRegistry.get(sessionId).orElse(null);

        if (wsSession == null || !wsSession.isActive()) {
            log.warn("No active WS session for sessionId={}, dropping message", sessionId);
            return true;
        }

        if ("preview".equals(taskType)) {
            return handlePreview(wsSession, sessionId, message);
        } else {
            return handleDetailed(wsSession, sessionId, message);
        }
    }

    private boolean handlePreview(WsSession wsSession, String sessionId, Message message) throws Exception {
        PreviewResult result = objectMapper.readValue(message.getBody(), PreviewResult.class);

        if (result.error() != null) {
            sendToClient(wsSession, WsOutboundMessage.error("PARSER_ERROR", result.error()));
            return true;
        }

        if (result.isDone()) {
            log.info("Preview parsing done for sessionId={}", sessionId);

            WsOutboundMessage outbound = WsOutboundMessage.finish("Parsing finished.");
            sendToClient(wsSession, outbound);

            return true;
        }

        WsOutboundMessage outbound = WsOutboundMessage.previewChunk(result.products());
        sendToClient(wsSession, outbound);

        return false;
    }

    private boolean handleDetailed(WsSession wsSession, String sessionId, Message message) throws Exception {
        DetailedResult result = objectMapper.readValue(message.getBody(), DetailedResult.class);

        if (result.error() != null) {
            sendToClient(wsSession, WsOutboundMessage.error("PARSER_ERROR", result.error()));
            return true;
        }

        if (result.isDone()) {
            log.info("Detailed parsing done for sessionId={}", sessionId);

            WsOutboundMessage outbound = WsOutboundMessage.finish("Parsing finished.");
            sendToClient(wsSession, outbound);

            return true;
        }

        WsOutboundMessage outbound = WsOutboundMessage.detailedResult(result);
        sendToClient(wsSession, outbound);

        return false;
    }

    private void sendToClient(WsSession wsSession, WsOutboundMessage outbound) throws Exception {
        if (!wsSession.isActive()) return;

        String json = new ObjectMapper().writeValueAsString(outbound);
        synchronized (wsSession.getSocket()) {
            wsSession.getSocket().sendMessage(new TextMessage(json));
        }
    }

//    private void stopSession(WsSession wsSession, CloseStatus closeStatus) throws Exception {
//        wsSession.getSocket().close(closeStatus);
//        wsSession.markInactive();
//    }
}
