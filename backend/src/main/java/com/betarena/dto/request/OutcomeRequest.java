package com.betarena.dto.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corps de requête pour créer ou modifier une issue isolément (hors du flow
 * normal de création d'événement, qui crée les issues en cascade).
 *
 * Les champs attendus : eventId (événement parent), label, odds (cote, doit
 * être strictement supérieure à 1.0) et isWinner. En général isWinner vaut
 * false à la création, il n'est mis à true qu'au moment de la résolution.
 */
public class OutcomeRequest {

    @JsonProperty(required = true)
    private int eventId;

    @JsonProperty(required = true)
    private String label;

    @JsonProperty(required = true)
    private BigDecimal odds;

    @JsonProperty(required = true)
    private boolean isWinner;

    public int getEventId() {
        return eventId;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public boolean isWinner() {
        return isWinner;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public void setWinner(boolean winner) {
        isWinner = winner;
    }

}
