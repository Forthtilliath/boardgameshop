package com.bgs.boardgameshop.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Anti-brute-force minimaliste sur {@code POST /api/auth/login} : au-delà de
 * {@value #MAX_ATTEMPTS} échecs pour une même IP dans la fenêtre glissante de
 * {@link #WINDOW}, les tentatives suivantes sont rejetées pendant
 * {@link #LOCKOUT} sans même vérifier le mot de passe.
 *
 * <p>Stockage en mémoire (une seule instance backend, pas de reverse proxy dans
 * ce projet donc {@code request.getRemoteAddr()} suffit — pas besoin de gérer
 * {@code X-Forwarded-For}, qui serait spoofable sans proxy de confiance en amont).
 * Nettoyage paresseux : une entrée expirée est simplement écrasée à la prochaine
 * tentative, pas de tâche planifiée (volume trop faible pour en avoir besoin).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempts> attemptsByIp = new ConcurrentHashMap<>();

    public void checkAllowed(String ip) {
        Attempts attempts = attemptsByIp.get(ip);
        if (attempts != null && attempts.isLockedOut()) {
            throw new TooManyLoginAttemptsException();
        }
    }

    public void recordFailure(String ip) {
        attemptsByIp.compute(ip, (key, existing) -> {
            Attempts attempts = (existing == null || existing.windowExpired()) ? new Attempts() : existing;
            attempts.count++;
            if (attempts.count >= MAX_ATTEMPTS) {
                attempts.lockedUntil = Instant.now().plus(LOCKOUT);
            }
            return attempts;
        });
    }

    public void recordSuccess(String ip) {
        attemptsByIp.remove(ip);
    }

    private static final class Attempts {
        private final Instant windowStart = Instant.now();
        private int count = 0;
        private Instant lockedUntil = null;

        boolean isLockedOut() {
            return lockedUntil != null && Instant.now().isBefore(lockedUntil);
        }

        boolean windowExpired() {
            return lockedUntil == null && Instant.now().isAfter(windowStart.plus(WINDOW));
        }
    }
}
