package com.betarena.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.betarena.models.Bet;
import com.betarena.enums.BetStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Projection d'un pari (Bet).
 *
 * Le champ newBalance n'est renseigné que sur la réponse de POST /api/bets,
 * il est omis du JSON pour GET /api/bets/me grâce à l'annotation
 * JsonInclude.Include.NON_NULL.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BetResponse(
        int id,
        int eventId,
        String eventTitle,
        int outcomeId,
        String outcomeLabel,
        BigDecimal odds,
        BigDecimal amount,
        BigDecimal potentialGain,
        BetStatus status,
        @JsonSerialize(converter = InstantConverter.class) Instant placedAt,
        BigDecimal newBalance
) {

    /** Projection sans solde, utilisée par GET /api/bets/me. */
    public static BetResponse from(Bet bet) {
        return from(bet, null);
    }

    /**
     * Projection complète, avec le nouveau solde du parieur. Utilisée par
     * POST /api/bets juste après le débit. Passer null pour newBalance si on
     * ne veut pas l'inclure dans la réponse.
     */
    public static BetResponse from(Bet bet, BigDecimal newBalance) {
        return new BetResponse(
                bet.getId(),
                bet.getOutcome().getEvent().getId(),
                bet.getOutcome().getEvent().getTitle(),
                bet.getOutcome().getId(),
                bet.getOutcome().getLabel(),
                bet.getOutcome().getOdds(),
                bet.getAmount(),
                bet.getPotentialGain(),
                bet.getStatus(),
                bet.getPlacedAt(),
                newBalance
        );
    }

}
