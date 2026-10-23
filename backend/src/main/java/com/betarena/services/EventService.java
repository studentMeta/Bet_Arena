package com.betarena.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.betarena.dto.request.EventRequest;
import com.betarena.models.Bet;
import com.betarena.models.Event;
import com.betarena.models.Outcome;
import com.betarena.models.User;
import com.betarena.enums.BetStatus;
import com.betarena.enums.EventStatus;
import com.betarena.errors.EventError;
import com.betarena.dao.BetRepository;
import com.betarena.dao.EventRepository;
import com.betarena.dao.OutcomeRepository;
import com.betarena.dao.UserRepository;

/**
 * Logique métier autour de l'événement : création, clôture, résolution, ainsi
 * que les agrégats (volume total, totaux par issue) utilisés pour enrichir les
 * DTOs renvoyés au frontend.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OutcomeRepository outcomeRepository;
    private final BetRepository betRepository;
    private final UserService userService;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            OutcomeRepository outcomeRepository,
            BetRepository betRepository,
            UserService userService
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.outcomeRepository = outcomeRepository;
        this.betRepository = betRepository;
        this.userService = userService;
    }

    /**
     * Résultat de resolveEvent, renvoyé au contrôleur pour qu'il construise
     * son DTO de réponse.
     */
    public record ResolveOutcome(Event event, Outcome winningOutcome, BigDecimal totalDistributed) {
    }

    /**
     * Levée quand on tente une opération incompatible avec l'état actuel de
     * l'événement : clôturer un événement déjà clôturé ou résolu, résoudre un
     * événement encore ouvert, etc. Mappée sur HTTP 409 par les contrôleurs.
     */
    public static class IllegalEventStateException extends RuntimeException {
        public IllegalEventStateException(String message) {
            super(message);
        }
    }

    /**
     * Levée quand un EventRequest ne passe pas la validation (titre vide ou
     * trop long, date de clôture dans le passé, etc.). Mappée sur HTTP 422
     * par les contrôleurs.
     */
    public static class InvalidEventException extends RuntimeException {

        private final EventError exceptionDetails;

        public InvalidEventException(EventError exceptionDetails) {
            this.exceptionDetails = exceptionDetails;
        }

        public EventError getExceptionDetails() {
            return exceptionDetails;
        }

    }

    /**
     * Crée un événement après validation et le persiste avec ses issues en
     * cascade.
     *
     * Quelle que soit la valeur de status envoyée par le client, on force OPEN :
     * le statut ne se modifie ensuite qu'à travers closeEvent ou resolveEvent.
     */
    @Transactional
    public Event createEvent(int bookmakerId, EventRequest eventRequest) throws InvalidEventException {
        EventError error = validateEvent(eventRequest);

        if (error != null) throw new InvalidEventException(error);

        User bookmaker = new User();
        bookmaker.setId(bookmakerId);

        Event event = new Event();

        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setCategory(eventRequest.getCategory());
        event.setClosedAt(eventRequest.getClosedAt());
        event.setStatus(EventStatus.OPEN);
        event.setBookmaker(bookmaker);

        // Pose la référence inverse Outcome -> Event sur chaque issue,
        // sans quoi la cascade JPA n'aurait pas de quoi remplir event_id.
        List<Outcome> outcomes = eventRequest.getOutcomes() != null ? eventRequest.getOutcomes() : List.of();
        for (Outcome outcome : outcomes) {
            outcome.setEvent(event);
            outcome.setWinner(false);
        }
        event.setOutcomes(outcomes);

        return eventRepository.save(event);
    }

    /** Suppression dure, non exposée via l'API publique. */
    public Event deleteEvent(int id) {
        Event event = getEventById(id);

        if (event != null) eventRepository.deleteById(id);

        return event;
    }

    /**
     * Mise à jour d'un événement existant (titre, description, dates, etc.).
     *
     * Lève Exception si l'événement n'existe pas, InvalidEventException si
     * la nouvelle version ne passe pas la validation.
     */
    public Event updateEvent(int id, EventRequest eventRequest) throws Exception, InvalidEventException {
        EventError error = validateEvent(eventRequest);

        if (error != null) throw new InvalidEventException(error);

        Event event = getEventById(id);

        if (event == null) throw new Exception("Event not found.");

        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setCategory(eventRequest.getCategory());
        event.setClosedAt(eventRequest.getClosedAt());
        event.setStatus(eventRequest.getStatus());
        event.setOutcomes(eventRequest.getOutcomes());

        return eventRepository.save(event);
    }

    /**
     * Clôture manuelle d'un événement. Refuse de clôturer un événement
     * qui n'est plus dans l'état OPEN pour éviter de réécrire l'historique.
     *
     * Lève NoSuchElementException si l'événement n'existe pas,
     * IllegalEventStateException si son statut courant n'est pas OPEN.
     */
    @Transactional
    public Event closeEvent(int id) {
        Event event = getEventById(id);

        if (event == null) throw new NoSuchElementException("Event not found.");
        if (event.getStatus() != EventStatus.OPEN) {
            throw new IllegalEventStateException("Event is not OPEN.");
        }

        event.setStatus(EventStatus.CLOSED);

        return eventRepository.save(event);
    }

    /**
     * Tâche planifiée toutes les minutes : passe automatiquement à CLOSED les
     * événements dont la date limite est dépassée. 
     * 
     * On exige une clôture serveur quoi qu'il arrive, même si le bookmaker 
     * ne fait rien.
     */
    @Scheduled(fixedRate = 60000)
    public List<Event> autoCloseEvents() {
        List<Event> openEvents = eventRepository.findByStatus(EventStatus.OPEN);

        for (Event event : openEvents) {
            if (event.getClosedAt().isBefore(Instant.now())) {
                event.setStatus(EventStatus.CLOSED);
                eventRepository.save(event);
            }
        }

        return openEvents;
    }

    /**
     * Résout atomiquement un événement clôturé.
     *
     * En une seule transaction :
     *  - l'issue gagnante voit son flag isWinner passer à true
     *  - chaque pari placé sur l'issue gagnante est marqué WON et son parieur 
     *    crédité de mise multipliée par cote
     *  - les autres paris passent à LOST
     *  - l'événement est marqué RESOLVED et son resolvedAt daté.
     *
     * Si quoi que ce soit échoue en route toute la transaction est annulée.
     *
     * Lève NoSuchElementException si l'événement ou l'issue n'existe pas, ou
     * si l'issue ne fait pas partie de cet événement. Lève
     * IllegalEventStateException si l'événement n'est pas dans l'état CLOSED.
     */
    @Transactional
    public ResolveOutcome resolveEvent(int eventId, int winningOutcomeId) {
        Event event = getEventById(eventId);
        if (event == null) throw new NoSuchElementException("Event not found.");
        if (event.getStatus() != EventStatus.CLOSED) {
            throw new IllegalEventStateException("Event must be CLOSED to be resolved.");
        }

        Outcome winningOutcome = null;
        for (Outcome outcome : outcomeRepository.findByEvent(eventId)) {
            if (outcome.getId() == winningOutcomeId) {
                winningOutcome = outcome;
                break;
            }
        }
        if (winningOutcome == null) {
            throw new NoSuchElementException("Winning outcome does not belong to this event.");
        }

        BigDecimal totalDistributed = BigDecimal.ZERO;

        for (Outcome outcome : outcomeRepository.findByEvent(eventId)) {
            boolean isWinner = outcome.getId() == winningOutcomeId;
            outcome.setWinner(isWinner);
            outcomeRepository.save(outcome);

            for (Bet bet : betRepository.findByOutcome(outcome.getId())) {
                if (isWinner) {
                    BigDecimal gain = bet.getAmount().multiply(outcome.getOdds());
                    userService.updateUserBalance(bet.getBettor().getId(), gain);
                    bet.setStatus(BetStatus.WON);
                    totalDistributed = totalDistributed.add(gain);
                } else {
                    bet.setStatus(BetStatus.LOST);
                }
                betRepository.save(bet);
            }
        }

        event.setStatus(EventStatus.RESOLVED);
        event.setResolvedAt(Instant.now());
        event = eventRepository.save(event);

        return new ResolveOutcome(event, winningOutcome, totalDistributed);
    }

    /** Renvoie l'événement ou null si l'id ne correspond à rien. */
    public Event getEventById(int id) {
        return eventRepository.findById(id).orElse(null);
    }

    /**
     * Liste paginée d'événements, avec deux filtres optionnels. Le tri est
     * porté par le Pageable passé en argument : les contrôleurs le
     * construisent à partir du paramètre sort du contrat.
     *
     * status et category peuvent être null indépendamment l'un de l'autre,
     * auquel cas le filtre correspondant n'est pas appliqué.
     */
    public Page<Event> listEvents(EventStatus status, String category, Pageable pageable) {
        if (status != null && category != null) {
            return eventRepository.findByStatusAndCategory(status, category, pageable);
        }
        if (status != null) {
            return eventRepository.findByStatus(status, pageable);
        }
        if (category != null) {
            return eventRepository.findByCategory(category, pageable);
        }
        return eventRepository.findAll(pageable);
    }

    /** Volume total misé sur un événement, en sommant les mises de toutes ses issues. */
    public BigDecimal totalVolumeForEvent(int eventId) {
        BigDecimal value = betRepository.totalVolumeForEvent(eventId);
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * Volume misé par issue pour l'événement donné, sous forme de map
     * outcomeId vers total. Utilisé pour remplir totalBets sur chaque
     * OutcomeDto au moment de la sérialisation.
     */
    public Map<Integer, BigDecimal> outcomeTotalsForEvent(Event event) {
        Map<Integer, BigDecimal> totals = new HashMap<>();
        if (event.getOutcomes() == null) return totals;

        for (Outcome outcome : event.getOutcomes()) {
            BigDecimal amount = betRepository.totalAmountForOutcome(outcome.getId());
            totals.put(outcome.getId(), amount != null ? amount : BigDecimal.ZERO);
        }

        return totals;
    }

    /** Tous les événements créés par un bookmaker donné. */
    public List<Event> getEventsByBookmaker(int bookmakerId) {
        return eventRepository.findByBookmaker(bookmakerId);
    }

    /** Tous les événements dans un statut donné. */
    public List<Event> getEventsByStatus(EventStatus eventStatus) {
        return eventRepository.findByStatus(eventStatus);
    }

    /** Tous les événements (sans filtre). */
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    /** Compteur par statut, alimente le tableau de bord admin. */
    public long getEventCountByStatus(EventStatus eventStatus) {
        return eventRepository.countByStatus(eventStatus);
    }

    /** Compteur global d'événements. */
    public long getEventCount() {
        return eventRepository.count();
    }

    /** Vérifie qu'un id de bookmaker correspond à un utilisateur existant. */
    public boolean validateBookmaker(int bookmakerId) {
        return userRepository.existsById(bookmakerId);
    }

    /** Titre : non vide et au plus 200 caractères. */
    public boolean validateTitle(String title) {
        return title != null && ! title.isBlank() && title.length() <= 200;
    }

    /** Date de clôture : doit être strictement dans le futur. */
    public boolean validateClosureTime(Instant instant) {
        return instant.isAfter(Instant.now());
    }

    /**
     * Valide les champs d'un EventRequest. Renvoie null si tout est OK, sinon
     * un EventError qui détaille les champs en faute.
     */
    public EventError validateEvent(EventRequest eventRequest) {
        boolean validTitle = validateTitle(eventRequest.getTitle());
        boolean validClosureTime = validateClosureTime(eventRequest.getClosedAt());

        if (validTitle && validClosureTime) return null;

        EventError error = new EventError();

        error.setTitleInvalid(!validTitle);
        error.setClosureTimeInvalid(!validClosureTime);

        return error;
    }

}
