package chetmine.marketplace.parser.config;

import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.infrastructure.ws.SearchWebSocketHandler;
import chetmine.marketplace.parser.middleware.WsJwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@EnableWebSocket
@Configuration
@RequiredArgsConstructor
public class WebSocket implements WebSocketConfigurer {

    private final SearchWebSocketHandler searchWebSocketHandler;
    private final WsJwtAuthFilter wsJwtAuthFilter;

    @Override
    public void registerWebSocketHandlers(
            WebSocketHandlerRegistry webSocketHandlerRegistry)
    {
        log.info(">>> Registering WebSocket handlers");
        webSocketHandlerRegistry
                .addHandler(searchWebSocketHandler, "/ws/search")
                .addInterceptors(wsJwtAuthFilter)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(
                            ServerHttpRequest request,
                            WebSocketHandler wsHandler,
                            Map<String, Object> attributes) {
                        User user = (User) attributes.get("user");
                        return (Principal) user;
                    }
                })
                .setAllowedOrigins("http://localhost:3000");
    }
}
