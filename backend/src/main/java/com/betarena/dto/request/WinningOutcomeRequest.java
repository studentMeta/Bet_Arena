package com.betarena.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corps de requête de l'endpoint PATCH /api/events/{id}/resolve.
 *
 * Un seul champ : winningOutcomeId, l'identifiant de l'issue déclarée gagnante.
 */
public class WinningOutcomeRequest {

    @JsonProperty(required = true)
    private Integer winningOutcomeId;

    public Integer getWinningOutcomeId() {
        return winningOutcomeId;
    }

    public void setWinningOutcomeId(Integer winningOutcomeId) {
        this.winningOutcomeId = winningOutcomeId;
    }

}
