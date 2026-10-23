package com.betarena.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.betarena.models.Event;
import com.betarena.models.Outcome;
import com.betarena.enums.EventStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Projection d'un événement (Event).
 *
 * Les champs nuls (par exemple resolvedAt pour un événement encore ouvert)
 * sont omis du JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EventResponse(
        int id,
        String title,
        String description,
        String category,
        @JsonSerialize(converter = InstantConverter.class) Instant closeAt,
        EventStatus status,
        BigDecimal totalVolume,
        @JsonSerialize(converter = InstantConverter.class) Instant createdAt,
        @JsonSerialize(converter = InstantConverter.class) Instant resolvedAt,
        List<OutcomeResponse> outcomes
) {

    /**
     * Construit la projection à partir de l'entité. L'enssemble (volume total
     * misé sur l'événement, et volume par issue) est passés en paramètres,
     * c'est au service de les calculer en amont. On accepte null pour ces deux
     * paramètres : ils seront remplacés par zéro / liste vide.
     */
    public static EventResponse from(Event event, BigDecimal totalVolume, Map<Integer, BigDecimal> outcomeTotals) {
        List<Outcome> outcomes = event.getOutcomes() == null ? List.of() : event.getOutcomes();
        List<OutcomeResponse> outcomeResponses = outcomes.stream()
                .map(o -> OutcomeResponse.from(o, outcomeTotals != null ? outcomeTotals.get(o.getId()) : null))
                .toList();

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getClosedAt(),
                event.getStatus(),
                totalVolume != null ? totalVolume : BigDecimal.ZERO,
                event.getCreatedAt(),
                event.getResolvedAt(),
                outcomeResponses
        );
    }

}
