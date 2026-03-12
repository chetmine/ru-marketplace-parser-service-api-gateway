package chetmine.marketplace.parser.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Builder
public class WsSession {
    private final String wsSessionId;
    private final String userSessionId;
    private final Long userId;
    private final WebSocketSession socket;
    private final String taskType;

    @Builder.Default
    private final AtomicBoolean active = new AtomicBoolean(true);

    public boolean isActive() {
        return active.get() && socket.isOpen();
    }

    public void markInactive() {
        active.set(false);
    }
}
