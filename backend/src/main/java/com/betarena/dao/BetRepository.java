package com.betarena.dao;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.betarena.models.Bet;
import com.betarena.enums.BetStatus;

/**
 * Accès JPA à la table bets.
 *
 * En plus du CRUD habituel, ce repository expose quelques fonctions utiles aux
 * stats admin et au calcul des volumes affichés sur le détail d'un événement.
 */
@Repository
public interface BetRepository extends JpaRepository<Bet, Integer> {

    /** Tous les paris d'un parieur (sans pagination). */
    @Query("SELECT b FROM bets b, users u WHERE b.bettor = u AND u.id = :bettorId")
    List<Bet> findByBettor(int bettorId);

    /** Tous les paris placés sur une issue donnée. */
    @Query("SELECT b FROM bets b, outcomes o WHERE b.outcome = o AND o.id = :outcomeId")
    List<Bet> findByOutcome(int outcomeId);

    /** Tous les paris dans un statut donné (utile en stats). */
    @Query("SELECT b FROM bets b WHERE b.status = :betStatus")
    List<Bet> findByStatus(BetStatus betStatus);

    /** Version paginée de findByBettor pour l'historique du profil. */
    @Query("SELECT b FROM bets b, users u WHERE b.bettor = u AND u.id = :bettorId")
    Page<Bet> findByBettor(int bettorId, Pageable pageable);

    /** Historique paginé filtré par statut (PENDING, WON, LOST). */
    @Query("SELECT b FROM bets b, users u WHERE b.bettor = u AND u.id = :bettorId AND b.status = :status")
    Page<Bet> findByBettorAndStatus(int bettorId, BetStatus status, Pageable pageable);

    /** Somme de toutes les mises (tous statuts confondus). Peut renvoyer null si aucune ligne. */
    @Query("SELECT SUM(b.amount) FROM bets b")
    BigDecimal totalAmount();

    /**
     * Somme des gains effectivement distribués : mise multipliée par cote pour
     * les paris placés sur des issues marquées gagnantes. Renvoie null tant
     * qu'aucun événement n'a été résolu.
     */
    @Query("SELECT SUM(b.amount * o.odds) FROM bets b, outcomes o WHERE b.outcome = o AND o.isWinner = true")
    BigDecimal totalDistributed();

    /** Volume total misé sur un événement, toutes issues confondues. */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM bets b, outcomes o WHERE b.outcome = o AND o.event.id = :eventId")
    BigDecimal totalVolumeForEvent(int eventId);

    /** Volume misé sur une seule issue. */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM bets b WHERE b.outcome.id = :outcomeId")
    BigDecimal totalAmountForOutcome(int outcomeId);

}
