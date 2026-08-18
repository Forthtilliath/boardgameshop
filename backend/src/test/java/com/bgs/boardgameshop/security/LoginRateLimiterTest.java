package com.bgs.boardgameshop.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private final LoginRateLimiter rateLimiter = new LoginRateLimiter();

    @Test
    void allows_requests_below_the_failure_threshold() {
        String ip = "10.0.0.1";
        for (int i = 0; i < 4; i++) {
            assertThatCode(() -> rateLimiter.checkAllowed(ip)).doesNotThrowAnyException();
            rateLimiter.recordFailure(ip);
        }

        assertThatCode(() -> rateLimiter.checkAllowed(ip)).doesNotThrowAnyException();
    }

    @Test
    void locks_out_after_five_failures() {
        String ip = "10.0.0.2";
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(ip);
        }

        assertThatThrownBy(() -> rateLimiter.checkAllowed(ip))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void a_successful_login_resets_the_failure_count() {
        String ip = "10.0.0.3";
        for (int i = 0; i < 4; i++) {
            rateLimiter.recordFailure(ip);
        }
        rateLimiter.recordSuccess(ip);

        assertThatCode(() -> rateLimiter.checkAllowed(ip)).doesNotThrowAnyException();
    }

    @Test
    void lockout_is_scoped_per_ip() {
        String lockedOutIp = "10.0.0.4";
        String otherIp = "10.0.0.5";
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure(lockedOutIp);
        }

        assertThatThrownBy(() -> rateLimiter.checkAllowed(lockedOutIp))
                .isInstanceOf(TooManyLoginAttemptsException.class);
        assertThatCode(() -> rateLimiter.checkAllowed(otherIp)).doesNotThrowAnyException();
    }
}
