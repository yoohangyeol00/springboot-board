package com.board.backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(Long memberId, String loginId, String role) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("tokenType", "access")
                .claim("loginId", loginId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim("tokenType", "refresh")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public LoginMember getLoginMember(String token) {
        Claims claims = parseClaims(token);
        Long memberId = Long.valueOf(claims.getSubject());
        String loginId = claims.get("loginId", String.class);
        String role = claims.get("role", String.class);
        return new LoginMember(memberId, loginId, role);
    }

    public Long getMemberIdFromRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, "refresh");
        return Long.valueOf(claims.getSubject());
    }

    public boolean validateToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, "access");
        return true;
    }

    public boolean validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        validateTokenType(claims, "refresh");
        return true;
    }

    public LocalDateTime getRefreshTokenExpiresAt() {
        return LocalDateTime.now().plusNanos(refreshTokenExpiration * 1_000_000);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateTokenType(Claims claims, String expectedTokenType) {
        String tokenType = claims.get("tokenType", String.class);

        if (!expectedTokenType.equals(tokenType)) {
            throw new IllegalArgumentException("Invalid token type.");
        }
    }
}
