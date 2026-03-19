package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.dto.AuthTokensDTO;
import chetmine.marketplace.parser.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final String secretKey;

    public JwtService(@Value("${app.jwt.secret}") String secretKey) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public AuthTokensDTO issueTokens(User user) {
        String accessToken = buildToken(user, Duration.ofMinutes(15).toSeconds(), "access");
        String refreshToken = buildToken(user, Duration.ofDays(30).toSeconds(), "refresh");

        return new AuthTokensDTO(accessToken, refreshToken);
    }

    private String buildToken(User user, long expiration, String tokenType) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getId().toString())
                .claim("type", tokenType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expiration)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims validateAccessToken(String token) {
        Claims claims = parseClaims(token);

        if (Date.from(Instant.now()).after(claims.getExpiration())) {
            throw new JwtException("Expired access token");
        }

        if (!"access".equals(claims.get("type"))) {
            throw new JwtException("Not an access token");
        }

        return claims;
    }

    public Claims validateRefreshToken(String token) {
        Claims claims = parseClaims(token);

        if (Date.from(Instant.now()).after(claims.getExpiration())) {
            throw new JwtException("Expired access token");
        }

        if (!"refresh".equals(claims.get("type"))) {
            throw new JwtException("Not a refresh token");
        }

        return claims;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }
}
