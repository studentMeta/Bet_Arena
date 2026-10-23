package com.betarena.models;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.betarena.enums.BetStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Pari placé par un parieur sur une issue d'un événement.
 *
 * Le potentialGain (mise multipliée par cote) est figé à la création. C'est
 * volontaire : même si la cote de l'issue change ensuite, le gain théorique du
 * parieur reste garanti par le snapshot pris au moment du pari.
 *
 * Statut initial : PENDING. La résolution de l'événement le bascule sur WON
 * ou LOST en une seule transaction (voir EventService).
 */
@Entity(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Parieur, masqué du JSON pour ne pas exposer son profil dans la liste des paris. */
    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User bettor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BetStatus status;

    @ManyToOne()
    @JoinColumn(name = "outcome_id", nullable = false)
    private Outcome outcome;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /** Mise multipliée par la cote, calculé une fois pour toutes au moment du pari. */
    @Column(name = "potential_gain", nullable = false)
    private BigDecimal potentialGain;

    @JsonSerialize(converter = InstantConverter.class)
    @CreationTimestamp
    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    public int getId() {
        return id;
    }

    public User getBettor() {
        return bettor;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getPotentialGain() {
        return potentialGain;
    }

    public BetStatus getStatus() {
        return status;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBettor(User bettor) {
        this.bettor = bettor;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPotentialGain(BigDecimal potentialGain) {
        this.potentialGain = potentialGain;
    }

    public void setStatus(BetStatus status) {
        this.status = status;
    }

    public void setPlacedAt(Instant placedAt) {
        this.placedAt = placedAt;
    }

}
