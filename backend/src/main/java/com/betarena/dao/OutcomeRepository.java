package com.betarena.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.betarena.models.Outcome;

/**
 * Accès JPA à la table outcomes.
 *
 * Les issues sont en général chargées en cascade via Event, ce repository
 * sert surtout aux opérations ponctuelles : récupérer une issue par id, ou
 * lister celles d'un événement précis.
 */
@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, Integer> {

    /** Toutes les issues d'un événement, dans l'ordre d'insertion. */
    @Query("SELECT o FROM outcomes o, events e WHERE o.event = e AND e.id = :eventId")
    List<Outcome> findByEvent(int eventId);

}
