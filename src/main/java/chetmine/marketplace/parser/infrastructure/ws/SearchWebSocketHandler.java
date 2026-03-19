package chetmine.marketplace.parser.infrastructure.ws;

import chetmine.marketplace.parser.dto.parser.TaskParams;
import chetmine.marketplace.parser.dto.ws.WsInboundMessage;
import chetmine.marketplace.parser.dto.ws.WsOutboundMessage;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.infrastructure.parser.ParserQueueManager;
import chetmine.marketplace.parser.exception.RequestLimitExceededException;
import chetmine.marketplace.parser.model.WsSession;
import chetmine.marketplace.parser.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final WsSessionRegistry wsSessionRegistry;
    private final SubscriptionService subscriptionService;
//    private final SearchCacheService searchCacheService;
//    private final SearchHistoryService searchHistoryService;
    private final ParserQueueManager parserQueueManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession socket) {
        log.info("WS connection established: socketId={}", socket.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession socket, TextMessage message) throws Exception {
        WsInboundMessage inbound;
        try {
            inbound = objectMapper.readValue(message.getPayload(), WsInboundMessage.class);
        } catch (Exception e) {
            sendError(socket, "INVALID_MESSAGE", null);
            return;
        }

        if (inbound.getType() == null || inbound.getQuery() == null || inbound.getQuery().isBlank()) {
            sendError(socket, "INVALID_MESSAGE", null);
            return;
        }

        Principal principal = socket.getPrincipal();
        if (principal == null) {
            sendError(socket, "UNAUTHORIZED", null);
            socket.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        User user = (User) principal;

        String taskType = inbound.getType();
        String query = inbound.getQuery();
        TaskParams params = inbound.getParams();

//        if ("preview".equals(taskType)) {
//            Optional<PreviewResult> cached = searchCacheService.getPreview(user.getId(), query);
//            if (cached.isPresent()) {
//                log.debug("Cache hit preview for userId={}, query={}", user.getId(), query);
//                send(socket, WsOutboundMessage.previewChunk(cached.get().getProducts(), true));
//                return;
//            }
//        } else if ("detailed".equals(taskType)) {
//            Optional<DetailedResult> cached = searchCacheService.getDetailed(user.getId(), query);
//            if (cached.isPresent()) {
//                log.debug("Cache hit detailed for userId={}, query={}", user.getId(), query);
//                send(socket, WsOutboundMessage.detailedResult(cached.get(), true));
//                return;
//            }
//        }

        try {
            subscriptionService.consume(user.getId());
        } catch (RequestLimitExceededException e) {
            sendError(socket, "LIMIT_EXCEEDED", e.getMessage());
            socket.close(CloseStatus.NORMAL);
            return;
        }

        if ("preview".equals(taskType)) {
//            searchHistoryService.save(user.getId(), query, taskType);
        }

        WsSession wsSession = WsSession.builder()
                .wsSessionId(socket.getId())
                .userSessionId(user.getSessionId())
                .userId(user.getId())
                .socket(socket)
                .taskType(taskType)
                .build();

        wsSessionRegistry.register(wsSession);

        parserQueueManager.startParsingSession(user.getSessionId(), taskType, query, params);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession socket, CloseStatus status) {
        log.info("WS connection closed: socketId={}, status={}", socket.getId(), status);

        Principal principal = socket.getPrincipal();
        if (principal == null) return;

        User user = (User) principal;
        String userSessionId = user.getSessionId();
        wsSessionRegistry.get(userSessionId).ifPresent(ws -> {
            parserQueueManager.stopParsingSession(userSessionId, ws.getTaskType());
            wsSessionRegistry.remove(userSessionId);
        });
    }

    @Override
    public void handleTransportError(WebSocketSession socket, Throwable exception) throws Exception {
        log.error("WS transport error: socketId={}", socket.getId(), exception);
        socket.close(CloseStatus.SERVER_ERROR);
    }

    private void send(WebSocketSession socket, WsOutboundMessage message) throws Exception {
        String json = objectMapper.writeValueAsString(message);
        synchronized (socket) {
            if (socket.isOpen()) {
                socket.sendMessage(new TextMessage(json));
            }
        }
    }

    private void sendError(WebSocketSession socket, String code, Object meta) {
        try {
            send(socket, WsOutboundMessage.error(code, meta));
        } catch (Exception e) {
            log.error("Failed to send error message to socket={}", socket.getId(), e);
        }
    }
}
