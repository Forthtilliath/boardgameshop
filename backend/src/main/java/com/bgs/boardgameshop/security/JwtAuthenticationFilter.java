package com.bgs.boardgameshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Authentifie chaque requête à partir de l'access token posé en cookie HttpOnly
 * par {@link AuthCookieService} (plus de header {@code Authorization} : le
 * navigateur envoie le cookie automatiquement, {@code JwtService} rejette tout
 * token qui ne serait pas de type "access" — voir {@link JwtService}).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthCookieService authCookieService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            AuthCookieService authCookieService
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authCookieService = authCookieService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Optional<String> token = authCookieService.readAccessToken(request);

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtService.extractEmail(token.get());
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isAccessTokenValid(token.get(), userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Token invalide/expire : on laisse la requete continuer sans authentification,
                // les regles d'autorisation de SecurityConfig se chargeront de la rejeter si besoin.
            }
        }

        filterChain.doFilter(request, response);
    }
}
