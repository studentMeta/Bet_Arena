package com.betarena.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Enveloppe paginée générique.
 *
 * Le JSON renvoyé contient cinq champs : content (résultats),
 * totalElements, totalPages, page (numéro de page 0-indexé) et size.
 *
 * Le paramètre de type T est le type des éléments dans content.
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {

    /** Construit une enveloppe directement à partir d'une Page Spring Data. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    /**
     * Comme la version précédente, mais en appliquant une projection sur chaque
     * élément avant de l'inclure dans le content. Pratique pour transformer une
     * Page d'entités en Page de DTOs sans recalculer la pagination.
     */
    public static <S, T> PageResponse<T> from(Page<S> page, java.util.function.Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

    /**
     * Construit l'enveloppe à partir d'une liste déjà projetée et des
     * informations de pagination correspondantes. Le totalPages est recalculé
     * à partir de totalElements et size.
     */
    public static <T> PageResponse<T> of(List<T> content, long totalElements, int page, int size) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, totalElements, totalPages, page, size);
    }

}
