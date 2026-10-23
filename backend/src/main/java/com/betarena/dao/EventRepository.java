package com.betarena.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.betarena.models.Event;
import com.betarena.enums.EventStatus;

/**
 * Accès JPA à la table events.
 *
 * Les méthodes nommées (sans annotation @Query) sont implémentées
 * automatiquement par Spring Data à partir de leur signature.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

    /** Tous les événements créés par un bookmaker (utile pour un futur dashboard perso). */
    @Query("SELECT e FROM events e, users u WHERE e.bookmaker = u AND u.id = :bookmakerId")
    List<Event> findByBookmaker(int bookmakerId);

    /** Liste non paginée utilisée par le scheduler de clôture automatique. */
    @Query("SELECT e FROM events e WHERE e.status = :eventStatus")
    List<Event> findByStatus(EventStatus eventStatus);

    /** Liste paginée d'événements dans un statut. */
    Page<Event> findByStatus(EventStatus eventStatus, Pageable pageable);

    /** Combine filtre par statut et par catégorie. */
    Page<Event> findByStatusAndCategory(EventStatus eventStatus, String category, Pageable pageable);

    /** Filtre uniquement par catégorie (tous statuts). */
    Page<Event> findByCategory(String category, Pageable pageable);

    /** Compteur utilisé par les stats admin. */
    @Query("SELECT COUNT(e) FROM events e WHERE e.status = :eventStatus")
    long countByStatus(EventStatus eventStatus);

}
