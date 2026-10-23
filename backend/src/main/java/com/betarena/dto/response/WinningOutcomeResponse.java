package com.betarena.dto.response;

import java.math.BigDecimal;

import com.betarena.models.Outcome;

/**
 * Représentation compacte de l'issue gagnante, intégrée dans la réponse de
 * résolution d'un événement. On ne renvoie ni le flag isWinner ni
 * l'événement parent, juste l'id, le label et la cote qui a servi au calcul
 * des gains.
 */
public record WinningOutcomeResponse(int id, String label, BigDecimal odds) {

    public static WinningOutcomeResponse from(Outcome outcome) {
        return new WinningOutcomeResponse(outcome.getId(), outcome.getLabel(), outcome.getOdds());
    }

}
