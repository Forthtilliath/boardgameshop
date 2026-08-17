package com.bgs.boardgameshop.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.PermissionsPolicyHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Sécurité HTTP stateless (JWT posé en cookies HttpOnly, voir {@link AuthCookieService}).
 * Le CSRF est réactivé (contrairement à une API pure "Bearer token") car
 * l'authentification repose maintenant sur un cookie envoyé automatiquement par le
 * navigateur : sans CSRF, un site tiers pourrait déclencher des requêtes mutantes au
 * nom de l'utilisateur connecté.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // Cookie lisible en JS (withHttpOnlyFalse) : c'est le but du CSRF
                        // double-submit, Angular le relit et le renvoie dans le header
                        // X-XSRF-TOKEN (noms par defaut, coherents avec app.config.ts).
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // Handler "plain" (pas de protection BREACH par XOR) : necessaire
                        // pour qu'Angular puisse renvoyer tel quel le token lu dans le cookie.
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // Login/register : pas encore de session/cookie CSRF a proteger a ce
                        // stade. Webhook Stripe : appel serveur-a-serveur, jamais de cookie.
                        .ignoringRequestMatchers("/api/auth/login", "/api/auth/register", "/api/payments/webhook")
                )
                // Positionne APRES SessionManagementFilter : meme en STATELESS, ce filtre reste
                // actif et declenche CsrfAuthenticationStrategy des qu'il detecte une authentification
                // "neuve" non issue d'une session persistee (ce que fait JwtAuthenticationFilter a
                // CHAQUE requete, puisqu'il n'y a justement pas de session pour retenir un etat "deja
                // vu") : le cookie XSRF-TOKEN est donc regenere a chaque requete authentifiee.
                // Le forcer (CsrfCookieFilter) a se resoudre APRES cette rotation, plutot qu'avant,
                // garantit que le cookie renvoye au client est toujours celui que le serveur attendra
                // ensuite (repro/diagnostic via logs TRACE Spring Security). Cote double-submit-cookie,
                // une rotation par requete n'est pas un probleme : le client relit le cookie courant
                // juste avant chaque appel mutant.
                .addFilterAfter(new CsrfCookieFilter(), SessionManagementFilter.class)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .headers(headers -> headers
                        // API JSON pure, aucune page HTML servie par ce backend : policy volontairement stricte.
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // DSL .permissionsPolicy(...) deprecie (retrait prevu) : on pose le writer directement.
                        .addHeaderWriter(new PermissionsPolicyHeaderWriter("camera=(), microphone=(), geolocation=()"))
                        // X-Content-Type-Options / X-Frame-Options : deja actives par defaut par Spring Security.
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/payments/webhook").permitAll()
                        // Regle specifique AVANT la regle generale GET /api/games/** ci-dessous :
                        // can-review a besoin de l'utilisateur connecte (@AuthenticationPrincipal).
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/games/*/reviews/can-review")
                        .authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/games/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tags").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/home").permitAll()
                        // Consultation d'une liste de favoris partagee : public par construction
                        // (c'est le but du lien), avant la regle generale /api/favorites/** ci-dessous.
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/favorites/shared/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // Indispensable pour que le navigateur envoie/accepte les cookies d'auth
        // (bgs_access_token, bgs_refresh_token, XSRF-TOKEN) sur les appels cross-port
        // 4200 -> 8080. Compatible avec allowedOrigins explicite (pas de "*").
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
