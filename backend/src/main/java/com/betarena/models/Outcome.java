package com.betarena.models;

import java.math.BigDecimal;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Une issue possible d'un événement.
 *
 * Un événement en a toujours au moins deux. La cote (odds) est figée à la
 * création, elle sert de référence pour calculer le potentialGain de chaque
 * pari placé sur cette issue.
 *
 * À la résolution de l'événement, exactement une issue voit son flag isWinner
 * passer à true, c'est ce qui déclenche la distribution des gains côté
 * EventService.
 */
@Entity(name = "outcomes")
public class Outcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Lien vers l'événement parent, masqué du JSON pour éviter une boucle de sérialisation. */
    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "label", nullable = false)
    private String label;

    /** Cote strictement supérieure à 1.0, garantie par la contrainte SQL chk_odds_positive. */
    @Column(name = "odds", nullable = false)
    private BigDecimal odds;

    /** Mis à true par EventService.resolveEvent sur l'issue gagnante. */
    @Column(name = "is_winner", nullable = false)
    private boolean isWinner;

    public int getId() {
        return id;
    }

    public Event getEvent() {
        return event;
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

    public void setId(int id) {
        this.id = id;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public void setWinner(boolean value) {
        isWinner = value;
    }

}
