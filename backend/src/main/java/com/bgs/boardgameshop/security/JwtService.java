package com.bgs.boardgameshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Émission et validation des JWT (HMAC/HS256), sans serveur d'authentification
 * externe : le backend signe et vérifie lui-même ses propres tokens.
 *
 * <p>Deux types de token, distingués par la claim {@code type} : un access token
 * (courte durée, utilisé pour authentifier chaque requête via {@link JwtAuthenticationFilter})
 * et un refresh token (longue durée, utilisé uniquement par {@code POST /api/auth/refresh}
 * pour réémettre un nouvel access token). Un refresh token ne doit jamais être accepté comme
 * access token, d'où la vérification systématique du type au décodage.
 */
@Service
public class JwtService {

    private static final String TYPE_CLAIM = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpirationMinutes;
    private final long refreshExpirationDays;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-minutes}") long accessExpirationMinutes,
            @Value("${jwt.refresh-expiration-days}") long refreshExpirationDays
    ) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessExpirationMinutes = accessExpirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, TYPE_ACCESS, accessExpirationMinutes, ChronoUnit.MINUTES);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, TYPE_REFRESH, refreshExpirationDays, ChronoUnit.DAYS);
    }

    public long getAccessExpirationMinutes() {
        return accessExpirationMinutes;
    }

    public long getRefreshExpirationDays() {
        return refreshExpirationDays;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, TYPE_ACCESS);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isTokenValid(token, userDetails, TYPE_REFRESH);
    }

    private boolean isTokenValid(String token, UserDetails userDetails, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            boolean sameUser = claims.getSubject().equals(userDetails.getUsername());
            boolean rightType = expectedType.equals(claims.get(TYPE_CLAIM, String.class));
            return sameUser && rightType && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String buildToken(UserDetails userDetails, String type, long amount, ChronoUnit unit) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(TYPE_CLAIM, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(amount, unit)))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
