package com.bgs.boardgameshop.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Enveloppe de pagination générique, plus sobre que de renvoyer directement un
 * {@link Page} Spring Data (qui expose des détails internes et déclenche un
 * avertissement au démarrage recommandant ce genre de DTO explicite).
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
