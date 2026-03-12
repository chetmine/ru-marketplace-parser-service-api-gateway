package chetmine.marketplace.parser.infrastructure.ws;


import chetmine.marketplace.parser.model.WsSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsSessionRegistry {
    private final ConcurrentHashMap<String, WsSession> sessions = new ConcurrentHashMap<>();

    public void register(WsSession session) {
        WsSession previous = sessions.put(session.getUserSessionId(), session);
        if (previous != null) {
            log.warn("Replaced existing WS session for userSessionId={}", session.getUserSessionId());
            previous.markInactive();
        }
        log.debug("Registered WS session for userSessionId={}", session.getUserSessionId());
    }

    public Optional<WsSession> get(String userSessionId) {
        return Optional.ofNullable(sessions.get(userSessionId));
    }

    public void remove(String userSessionId) {
        WsSession removed = sessions.remove(userSessionId);
        if (removed != null) {
            removed.markInactive();
            log.debug("Removed WS session for userSessionId={}", userSessionId);
        }
    }

    public boolean has(String userSessionId) {
        return sessions.containsKey(userSessionId);
    }
}
