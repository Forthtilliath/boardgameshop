package com.bgs.boardgameshop.auth;

import com.bgs.boardgameshop.security.AuthCookieService;
import com.bgs.boardgameshop.security.CustomUserDetailsService;
import com.bgs.boardgameshop.security.JwtService;
import com.bgs.boardgameshop.security.LoginRateLimiter;
import com.bgs.boardgameshop.security.SecurityUser;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.dto.LoginRequest;
import com.bgs.boardgameshop.user.dto.RegisterRequest;
import com.bgs.boardgameshop.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le JWT ne transite plus dans le corps JSON des réponses : {@code register}/{@code login}
 * posent directement des cookies HttpOnly (access + refresh) via {@link AuthCookieService},
 * invisibles depuis le JavaScript du frontend (immunisés contre le vol par XSS).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;
    private final LoginRateLimiter loginRateLimiter;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            AuthCookieService authCookieService,
            LoginRateLimiter loginRateLimiter,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        this.authService = authService;
        this.authCookieService = authCookieService;
        this.loginRateLimiter = loginRateLimiter;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        User user = authService.register(request);
        authCookieService.issueTokens(response, new SecurityUser(user));
        return UserResponse.fromEntity(user);
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String ip = httpRequest.getRemoteAddr();
        loginRateLimiter.checkAllowed(ip);

        User user;
        try {
            user = authService.login(request);
        } catch (BadCredentialsException e) {
            loginRateLimiter.recordFailure(ip);
            throw e;
        }

        loginRateLimiter.recordSuccess(ip);
        authCookieService.issueTokens(httpResponse, new SecurityUser(user));
        return UserResponse.fromEntity(user);
    }

    /** Renouvelle l'access token à partir du refresh token (rotation des deux cookies). */
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = authCookieService.readRefreshToken(request)
                .orElseThrow(() -> new BadCredentialsException("Session expirée, reconnectez-vous"));

        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("Session expirée, reconnectez-vous");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new BadCredentialsException("Session expirée, reconnectez-vous");
        }

        authCookieService.issueTokens(response, userDetails);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        authCookieService.clearTokens(response);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal SecurityUser principal) {
        // /api/auth/** est permitAll (login/register en ont besoin) : sans cookie
        // d'access token valide, principal est null ici plutot que rejete en amont.
        if (principal == null) {
            throw new BadCredentialsException("Non connecté");
        }
        return UserResponse.fromEntity(principal.getUser());
    }
}
