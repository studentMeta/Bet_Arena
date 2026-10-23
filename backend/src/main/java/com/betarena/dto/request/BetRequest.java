package com.betarena.dto.request;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Corps de la requête POST /api/bets.
 *
 * Le client envoie deux champs : outcomeId (l'identifiant de l'issue visée) et
 * amount (le montant misé, qui doit être strictement positif). La validation est
 * faite par BetController et BetService, ce DTO ne fait que transporter la
 * donnée jusque-là.
 */
public class BetRequest {

    @JsonProperty(required = true)
    private int outcomeId;

    @JsonProperty(required = true)
    private BigDecimal amount;

    public int getOutcomeId() {
        return outcomeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setOutcomeId(int outcomeId) {
        this.outcomeId = outcomeId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
