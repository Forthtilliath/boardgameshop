package com.bgs.boardgameshop.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Pose/lit les cookies d'authentification (access + refresh), tous deux HttpOnly
 * pour rester inaccessibles en JavaScript (donc immunisés contre le vol par XSS).
 *
 * <p>Le cookie refresh est scopé à {@code /api/auth} (au lieu de {@code /} comme
 * l'access token) : il n'est jamais envoyé sur les autres routes de l'API, ce qui
 * réduit sa surface d'exposition.
 *
 * <p>{@code Secure=true} fonctionne aussi en dev en HTTP simple : les navigateurs
 * traitent {@code localhost} comme un contexte sécurisé, donc le cookie est bien
 * posé/envoyé sur http://localhost. {@code SameSite=Lax} suffit (pas besoin de
 * {@code None}) car localhost:4200 et localhost:8080 sont "same-site" au sens de
 * l'algorithme SameSite (même domaine enregistrable, le port n'entre pas en compte).
 */
@Service
public class AuthCookieService {

    public static final String ACCESS_COOKIE = "bgs_access_token";
    public static final String REFRESH_COOKIE = "bgs_refresh_token";

    private final JwtService jwtService;

    public AuthCookieService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void issueTokens(HttpServletResponse response, UserDetails userDetails) {
        addCookie(response, ACCESS_COOKIE, jwtService.generateAccessToken(userDetails), "/",
                Duration.ofMinutes(jwtService.getAccessExpirationMinutes()));
        addCookie(response, REFRESH_COOKIE, jwtService.generateRefreshToken(userDetails), "/api/auth",
                Duration.ofDays(jwtService.getRefreshExpirationDays()));
    }

    public void clearTokens(HttpServletResponse response) {
        addCookie(response, ACCESS_COOKIE, "", "/", Duration.ZERO);
        addCookie(response, REFRESH_COOKIE, "", "/api/auth", Duration.ZERO);
    }

    public Optional<String> readAccessToken(HttpServletRequest request) {
        return readCookie(request, ACCESS_COOKIE);
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        return readCookie(request, REFRESH_COOKIE);
    }

    private Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return java.util.Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
