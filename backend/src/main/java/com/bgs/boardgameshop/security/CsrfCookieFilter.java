package com.bgs.boardgameshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Force le chargement (donc la pose du cookie {@code XSRF-TOKEN}) du token CSRF
 * sur chaque requête, y compris les GET publics.
 *
 * <p>Nécessaire pour une SPA : avec {@code CsrfTokenRequestAttributeHandler}
 * (voir {@link SecurityConfig}), Spring Security ne charge le token que si
 * quelque chose lit explicitement l'attribut {@code _csrf} de la requête — ce
 * qu'une vue Thymeleaf ferait automatiquement, mais pas une API JSON pure.
 * Sans ce filtre, le cookie ne serait jamais posé et Angular n'aurait jamais de
 * valeur à renvoyer dans le header {@code X-XSRF-TOKEN}. Pattern documenté
 * officiellement par Spring Security pour l'intégration CSRF + SPA.
 */
public final class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
