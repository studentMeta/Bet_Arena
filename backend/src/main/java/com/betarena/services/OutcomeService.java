package com.betarena.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.betarena.dto.request.OutcomeRequest;
import com.betarena.models.Event;
import com.betarena.models.Outcome;
import com.betarena.errors.OutcomeError;
import com.betarena.dao.EventRepository;
import com.betarena.dao.OutcomeRepository;

/**
 * Service utilitaire pour manipuler une issue isolément.
 *
 * En pratique les issues sont créées et persistées en cascade via
 * EventService.createEvent. Ce service reste utile pour récupérer une issue
 * depuis le BetController au moment du placement d'un pari, et pour quelques
 * opérations de maintenance qui ne passent pas par l'API publique.
 */
@Service
public class OutcomeService {

    private final EventRepository eventRepository;
    private final OutcomeRepository outcomeRepository;

    public OutcomeService(EventRepository eventRepository, OutcomeRepository outcomeRepository) {
        this.eventRepository = eventRepository;
        this.outcomeRepository = outcomeRepository;
    }

    /** Levée quand un OutcomeRequest ne passe pas la validation. */
    public static class InvalidOutcomeException extends RuntimeException {

        private final OutcomeError exceptionDetails;

        public InvalidOutcomeException(OutcomeError exceptionDetails) {
            this.exceptionDetails = exceptionDetails;
        }

        public OutcomeError getExceptionDetails() {
            return exceptionDetails;
        }

    }

    /**
     * Crée une issue rattachée à un événement existant.
     * EventService.createEvent en cascade. Lève InvalidOutcomeException si
     * l'événement n'existe pas ou si la cote est invalide.
     */
    public Outcome createOutcome(OutcomeRequest outcomeRequest) throws InvalidOutcomeException {
        OutcomeError error = validateOutcome(outcomeRequest);

        if (error != null) throw new InvalidOutcomeException(error);

        Event event = new Event();
        event.setId(outcomeRequest.getEventId());

        Outcome outcome = new Outcome();

        outcome.setEvent(event);
        outcome.setLabel(outcomeRequest.getLabel());
        outcome.setOdds(outcomeRequest.getOdds());
        outcome.setWinner(outcomeRequest.isWinner());

        return outcomeRepository.save(outcome);
    }

    /** Supprime une issue. */
    public Outcome deleteOutcome(int id) {
        Outcome outcome = getOutcomeById(id);

        if (outcome != null) outcomeRepository.deleteById(id);

        return outcome;
    }

    /**
     * Met à jour une issue existante (label, cote, statut gagnant).
     */
    public Outcome updateOutcome(int id, OutcomeRequest outcomeRequest) throws Exception, InvalidOutcomeException {
        OutcomeError error = validateOutcome(outcomeRequest);

        if (error != null) throw new InvalidOutcomeException(error);

        Event event = new Event();
        event.setId(outcomeRequest.getEventId());

        Outcome outcome = getOutcomeById(id);

        if (outcome == null) throw new Exception("Outcome not found.");

        outcome.setEvent(event);
        outcome.setLabel(outcomeRequest.getLabel());
        outcome.setOdds(outcomeRequest.getOdds());
        outcome.setWinner(outcomeRequest.isWinner());

        return outcomeRepository.save(outcome);
    }

    /** Renvoie l'issue ou null si l'id ne correspond à rien. */
    public Outcome getOutcomeById(int id) {
        return outcomeRepository.findById(id).orElse(null);
    }

    /** Toutes les issues attachées à un événement. */
    public List<Outcome> getOutcomesByEvent(int eventId) {
        return outcomeRepository.findByEvent(eventId);
    }

    /** Toutes les issues, sans filtre. */
    public List<Outcome> getAll() {
        return outcomeRepository.findAll();
    }

    /** Vérifie qu'un id d'événement correspond bien à une ligne existante. */
    public boolean validateEvent(int eventId) {
        return eventRepository.existsById(eventId);
    }

    /** Cote acceptable : strictement supérieure à 1.0. */
    public boolean validateOdds(BigDecimal odds) {
        return odds.compareTo(BigDecimal.ONE) > 0;
    }

    /**
     * Valide un OutcomeRequest. Renvoie null si tout est OK, sinon un
     * OutcomeError qui détaille les champs en faute.
     */
    public OutcomeError validateOutcome(OutcomeRequest outcomeRequest) {
        boolean validEvent = validateEvent(outcomeRequest.getEventId());
        boolean validOdds = validateOdds(outcomeRequest.getOdds());

        if (validEvent && validOdds) return null;

        OutcomeError error = new OutcomeError();

        error.setEventInvalid(!validEvent);
        error.setOddsInvalid(!validOdds);

        return error;
    }

}
