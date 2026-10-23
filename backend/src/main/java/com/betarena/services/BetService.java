package com.betarena.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.betarena.dto.request.BetRequest;
import com.betarena.models.Bet;
import com.betarena.models.Outcome;
import com.betarena.models.User;
import com.betarena.enums.BetStatus;
import com.betarena.errors.BetError;
import com.betarena.dao.BetRepository;
import com.betarena.dao.OutcomeRepository;
import com.betarena.dao.UserRepository;

/**
 * Opérations métier sur les paris : création, lecture paginée, marquage
 * WON/LOST, agrégats pour les stats.
 *
 * Le débit du solde et le placement du pari sont coordonnés par BetController
 * dans une seule transaction. Ce service ne gère que la création de l'entité
 * Bet, la cohérence du solde reste de la responsabilité de l'appelant.
 */
@Service
public class BetService {

    private final BetRepository betRepository;
    private final OutcomeRepository outcomeRepository;
    private final UserRepository userRepository;

    public BetService(BetRepository betRepository, OutcomeRepository outcomeRepository, UserRepository userRepository) {
        this.betRepository = betRepository;
        this.outcomeRepository = outcomeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Levée quand un BetRequest ne passe pas la validation (typiquement un
     * montant inférieur ou égal à zéro). Mappée sur HTTP 422 par le contrôleur.
     */
    public static class InvalidBetException extends RuntimeException {

        private final BetError exceptionDetails;

        public InvalidBetException(BetError exceptionDetails) {
            this.exceptionDetails = exceptionDetails;
        }

        public BetError getExceptionDetails() {
            return exceptionDetails;
        }

    }

    /**
     * Persiste un nouveau pari pour le parieur donné.
     *
     * Le potentialGain est calculé tout de suite (montant multiplié par cote)
     * et stocké en base. Il sert de référence si la cote de l'issue change
     * avant la résolution. Lève InvalidBetException si la validation échoue
     * (montant invalide ou issue inexistante).
     */
    public Bet createBet(int bettorId, BetRequest betRequest) throws InvalidBetException {
        BetError error = validateBet(betRequest);

        if (error != null) throw new InvalidBetException(error);

        User bettor = new User();
        bettor.setId(bettorId);

        Outcome outcome = outcomeRepository.findById(betRequest.getOutcomeId())
                .orElseThrow(() -> new IllegalArgumentException("L'Outcome avec l'ID " + betRequest.getOutcomeId() + " n'existe pas."));

        Bet bet = new Bet();

        bet.setAmount(betRequest.getAmount());
        bet.setStatus(BetStatus.PENDING);
        bet.setBettor(bettor);
        bet.setOutcome(outcome);
        bet.setPotentialGain(bet.getAmount().multiply(outcome.getOdds()));

        return betRepository.save(bet);
    }

    /** Suppression d'un pari (réservé aux tests/outillage). */
    public Bet deleteBet(int id) {
        Bet bet = getBetById(id);

        if (bet != null) betRepository.deleteById(id);

        return bet;
    }

    /**
     * Met à jour un pari existant (montant et issue). Non exposée publiquement :
     * en pratique on n'autorise pas la modification d'un pari déjà placé, mais
     * la méthode reste là pour les tests et l'outillage.
     */
    public Bet updateBet(int id, BetRequest betRequest) throws Exception, InvalidBetException {
        BetError error = validateBet(betRequest);
        if (error != null) throw new InvalidBetException(error);

        Outcome outcome = outcomeRepository.findById(betRequest.getOutcomeId())
                .orElseThrow(() -> new IllegalArgumentException("Outcome not found"));

        Bet bet = getBetById(id);

        if (bet == null) throw new Exception("Bet not found.");

        bet.setAmount(betRequest.getAmount());
        bet.setOutcome(outcome);
        bet.setPotentialGain(bet.getAmount().multiply(outcome.getOdds()));

        return betRepository.save(bet);
    }

    /** Bascule un pari en WON. Appelé par EventService lors de la résolution. */
    public Bet winBet(int id) throws Exception {
        Bet bet = getBetById(id);

        if (bet == null) throw new Exception("Bet not found.");

        bet.setStatus(BetStatus.WON);

        return betRepository.save(bet);
    }

    /** Bascule un pari en LOST. Appelé par EventService lors de la résolution. */
    public Bet loseBet(int id) throws Exception {
        Bet bet = getBetById(id);

        if (bet == null) throw new Exception("Bet not found.");

        bet.setStatus(BetStatus.LOST);

        return betRepository.save(bet);
    }

    /** Renvoie le pari ou null. */
    public Bet getBetById(int id) {
        return betRepository.findById(id).orElse(null);
    }

    /** Tous les paris d'un parieur - version non paginée (debug). */
    public List<Bet> getBetsByBettor(int bettorId) {
        return betRepository.findByBettor(bettorId);
    }

    /** Historique paginé du parieur (utilisé par GET /api/bets/me). */
    public Page<Bet> findByBettor(int bettorId, Pageable pageable) {
        return betRepository.findByBettor(bettorId, pageable);
    }

    /** Historique paginé du parieur filtré par statut (PENDING, WON, LOST). */
    public Page<Bet> findByBettorAndStatus(int bettorId, BetStatus status, Pageable pageable) {
        return betRepository.findByBettorAndStatus(bettorId, status, pageable);
    }

    /** Tous les paris placés sur une issue donnée. Utilisé par la résolution. */
    public List<Bet> getBetsByOutcome(int outcomeId) {
        return betRepository.findByOutcome(outcomeId);
    }

    /** Tous les paris dans un statut donné. */
    public List<Bet> getBetsByStatus(BetStatus betStatus) {
        return betRepository.findByStatus(betStatus);
    }

    /** Tous les paris du système. */
    public List<Bet> getAll() {
        return betRepository.findAll();
    }

    /** Nombre total de paris placés depuis le début. */
    public long getBetCount() {
        return betRepository.count();
    }

    /** Somme de toutes les mises (volume total). Peut renvoyer null sur base vide. */
    public BigDecimal getTotalAmount() {
        return betRepository.totalAmount();
    }

    /** Somme des gains effectivement distribués lors des résolutions passées. */
    public BigDecimal getTotalDistributed() {
        return betRepository.totalDistributed();
    }

    /** Confirme qu'un id de parieur correspond bien à un utilisateur existant. */
    public boolean validateBettor(int bettorId) {
        return userRepository.existsById(bettorId);
    }

    /** Confirme qu'une issue existe. */
    public boolean validateOutcome(int outcomeId) {
        return outcomeRepository.existsById(outcomeId);
    }

    /**
     * Valide un BetRequest : l'outcomeId doit pointer sur une issue existante
     * et amount doit être strictement positif. Renvoie null si tout est OK,
     * sinon un BetError avec le détail des champs en faute.
     */
    public BetError validateBet(BetRequest betRequest) {
        boolean validOutcome = validateOutcome(betRequest.getOutcomeId());
        boolean validAmount = betRequest.getAmount() != null
                && betRequest.getAmount().compareTo(BigDecimal.ZERO) > 0;

        if (validOutcome && validAmount) {
            return null;
        }

        BetError error = new BetError();
        error.setAmountInvalid(!validAmount);

        return error;
    }

}
