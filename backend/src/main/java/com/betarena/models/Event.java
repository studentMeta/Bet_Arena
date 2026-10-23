package com.betarena.models;

import java.time.Instant;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.betarena.enums.EventStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Événement ouvert aux paris : une question, une date limite, une liste d'issues.
 *
 * Cycle de vie : OPEN, puis CLOSED quand la date limite est dépassée ou que le
 * bookmaker clôture à la main, puis RESOLVED quand l'issue gagnante est désignée
 * et les gains distribués. Une fois résolu, l'événement n'évolue plus.
 *
 * Attention : le champ closedAt est exposé en JSON sous le nom closeAt pour coller
 * au contrat API, côté base de données, la colonne s'appelle close_at.
 */
@Entity(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    /** Date à laquelle l'événement cesse d'accepter des paris (passe à CLOSED). */
    @JsonProperty("closeAt")
    @JsonSerialize(converter = InstantConverter.class)
    @Column(name = "close_at", nullable = false)
    private Instant closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;

    /** Bookmaker qui a créé l'événement - masqué du JSON pour ne pas exposer son profil. */
    @JsonIgnore
    @ManyToOne()
    @JoinColumn(name = "created_by", nullable = false)
    private User bookmaker;

    @JsonSerialize(converter = InstantConverter.class)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Renseigné par EventService.resolveEvent au moment de la résolution. */
    @JsonSerialize(converter = InstantConverter.class)
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /** Issues possibles. La cascade ALL permet de persister les issues en même temps que l'événement. */
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Outcome> outcomes;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public EventStatus getStatus() {
        return status;
    }

    public User getBookmaker() {
        return bookmaker;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public void setBookmaker(User bookmaker) {
        this.bookmaker = bookmaker;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setOutcomes(List<Outcome> outcomes) {
        this.outcomes = outcomes;
    }

}