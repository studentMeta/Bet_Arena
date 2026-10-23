package com.betarena.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.annotation.JsonSerialize;

import com.betarena.enums.EventStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Réponse de l'endpoint PATCH /api/events/{id}/resolve.
 *
 * Délibérément différente d'un EventDto complet : on renvoie juste l'id de
 * l'événement, son nouveau statut, l'issue gagnante en version compacte, le
 * total des gains qui ont été distribués, et l'horodatage de résolution.
 */
public record ResolveResultResponse(
        int id,
        EventStatus status,
        WinningOutcomeResponse winningOutcome,
        BigDecimal totalDistributed,
        @JsonSerialize(converter = InstantConverter.class) Instant resolvedAt
) {
}
