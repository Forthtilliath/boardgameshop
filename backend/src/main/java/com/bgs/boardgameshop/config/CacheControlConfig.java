package com.bgs.boardgameshop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Cache-Control court sur les endpoints publics peu volatils (catalogue, tags,
 * page d'accueil) : évite de refrapper la base à chaque navigation alors que le
 * contenu change rarement à cette échelle de temps. Remplace le
 * {@code no-cache, no-store} posé par défaut par Spring Security
 * (HeaderWriterFilter, pensé pour du contenu authentifié) — l'interceptor MVC
 * s'exécute après la chaîne de filtres Security, donc {@code setHeader} écrase
 * bien la valeur par défaut plutôt que de s'y ajouter.
 */
@Configuration
public class CacheControlConfig implements WebMvcConfigurer {

    private static final String CACHE_CONTROL = "public, max-age=60";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("Cache-Control", CACHE_CONTROL);
                return true;
            }
        }).addPathPatterns("/api/games", "/api/games/*", "/api/games/*/related", "/api/games/categories", "/api/tags", "/api/home");
    }
}
