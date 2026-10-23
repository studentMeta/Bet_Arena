package com.betarena.dto.request;

import java.time.Instant;
import java.util.List;

import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.betarena.models.Outcome;
import com.betarena.enums.EventStatus;
import com.betarena.serialization.InstantConverter;

/**
 * Corps de la requête POST /api/events.
 *
 * Le client envoie le champ closeAt, en interne on garde le nom closedAt pour
 * rester aligné avec la colonne close_at de la base de données.
 *
 * Le champ outcomes attend au minimum deux issues, chacune avec un label et
 * une cote strictement supérieure à 1.0.
 */
public class EventRequest {

    @JsonProperty(required = true)
    private String title;

    private String description;

    private String category;

    @JsonSerialize(converter = InstantConverter.class)
    @JsonProperty(value = "closeAt", required = true)
    private Instant closedAt;

    /** Optionnel à la création (toujours forcé à OPEN côté service). */
    private EventStatus status;

    private List<Outcome> outcomes;

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

    public List<Outcome> getOutcomes() {
        return outcomes;
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

    public void setOutcomes(List<Outcome> outcomes) {
        this.outcomes = outcomes;
    }

}
