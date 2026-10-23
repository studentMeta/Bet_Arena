package com.betarena.dto.response;

import java.math.BigDecimal;

import com.betarena.models.Outcome;

/**
 * Projection d'une issue (Outcome).
 * Pas de référence à l'événement parent dans le JSON.
 */
public record OutcomeResponse(
        int id,
        String label,
        BigDecimal odds,
        BigDecimal totalBets
) {

    /**
     * Construit la projection à partir de l'entité et du volume total déjà misé
     * sur cette issue. Si totalBets est null, on renvoie zéro plutôt que de
     * propager le null.
     */
    public static OutcomeResponse from(Outcome outcome, BigDecimal totalBets) {
        return new OutcomeResponse(
                outcome.getId(),
                outcome.getLabel(),
                outcome.getOdds(),
                totalBets != null ? totalBets : BigDecimal.ZERO
        );
    }

}
