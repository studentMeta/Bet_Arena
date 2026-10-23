package com.betarena.serialization;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import tools.jackson.databind.util.StdConverter;

/**
 * Sérialise un Instant au format ISO 8601 UTC (par exemple
 * 2026-04-01T18:00:00Z).
 *
 * Toutes les dates exposées en JSON passent par ce convertisseur via
 * l'annotation JsonSerialize sur le champ correspondant.
 */
public class InstantConverter extends StdConverter<Instant, String> {

    @Override
    public String convert(Instant instant) {
        if (instant == null) return null;

        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

}
