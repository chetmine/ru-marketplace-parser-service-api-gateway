package chetmine.marketplace.parser.middleware;

import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.service.JwtService;
import chetmine.marketplace.parser.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WsJwtAuthFilter implements HandshakeInterceptor {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        //log.info(">>> beforeHandshake called: {}", request.getURI());

        List<String> tokens = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .get("token");

        if (tokens == null || tokens.isEmpty()) {
            log.warn("WS handshake rejected: no token provided");
            return false;
        }

        String token = tokens.get(0);

        try {

            Claims claims = jwtService.validateAccessToken(token);
            long userId = Long.parseLong(claims.getSubject());

            User user = userService.findById(userId);
            attributes.put("user", user);
            return true;
        } catch (JwtException e) {
            log.warn("WS handshake rejected: invalid token");
            return false;
        } catch (Exception e) {
            log.warn("WS handshake rejected: token validation error", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {}
}
