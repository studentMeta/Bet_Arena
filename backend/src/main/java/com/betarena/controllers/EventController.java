package com.betarena.controllers;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.betarena.dto.request.EventRequest;
import com.betarena.dto.request.WinningOutcomeRequest;
import com.betarena.dto.response.EventResponse;
import com.betarena.dto.response.PageResponse;
import com.betarena.dto.response.ResolveResultResponse;
import com.betarena.dto.response.WinningOutcomeResponse;
import com.betarena.models.Event;
import com.betarena.models.User;
import com.betarena.enums.EventStatus;
import com.betarena.enums.UserRole;
import com.betarena.services.EventService;
import com.betarena.services.UserService;

/**
 * Endpoints liés aux événements : liste, détail, création par
 * un bookmaker, clôture manuelle et résolution.
 *
 * Le contrôleur traduit les paramètres du contrat (page, size, sort) en
 * Pageable Spring Data avant de laisser EventService faire le travail. À la
 * résolution, il se contente de remettre le ResolveResultDto attendu en forme
 * à partir du ResolveOutcome renvoyé par le service.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private static final int DEFAULT_SIZE = 15;
    private static final int MAX_SIZE = 50;
    private static final String DEFAULT_SORT = "closeAt,asc";

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    /**
     * Liste paginée d'événements. Renvoie 400 si l'un des paramètres
     * de pagination ou de tri est hors limites.
     */
    @GetMapping
    public ResponseEntity<PageResponse<EventResponse>> list(
            @RequestParam(name = "status", required = false, defaultValue = "OPEN") String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "" + DEFAULT_SIZE) int size,
            @RequestParam(name = "sort", required = false, defaultValue = DEFAULT_SORT) String sort
    ) {
        EventStatus eventStatus = parseStatus(status);
        if (eventStatus == null) return ResponseEntity.status(400).build();

        Pageable pageable = buildPageable(page, size, sort);
        if (pageable == null) return ResponseEntity.status(400).build();

        Page<Event> events = eventService.listEvents(eventStatus, category, pageable);

        PageResponse<EventResponse> response = PageResponse.from(events, this::toDto);
        return ResponseEntity.ok(response);
    }

    /** Détail d'un événement. Renvoie 404 si l'id ne correspond à rien. */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getOne(@PathVariable("id") int id) {
        Event event = eventService.getEventById(id);
        if (event == null) return ResponseEntity.status(404).build();

        return ResponseEntity.ok(toDto(event));
    }

    /**
     * Création d'un événement. Réservé au rôle BOOKMAKER. Renvoie 422
     * si la validation métier échoue (titre, date, etc.).
     */
    @PostMapping
    public ResponseEntity<EventResponse> create(@RequestBody EventRequest eventRequest, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null || user.getRole() != UserRole.BOOKMAKER) return ResponseEntity.status(403).build();

        try {
            Event event = eventService.createEvent(user.getId(), eventRequest);
            return ResponseEntity.status(201).body(toDto(event));
        } catch (EventService.InvalidEventException e) {
            return ResponseEntity.status(422).build();
        }
    }

    /**
     * Clôture manuelle d'un événement. Réservé au rôle BOOKMAKER.
     * Renvoie 404 si l'événement n'existe pas, 409 s'il n'est plus dans
     * l'état OPEN.
     */
    @PatchMapping("/{id}/close")
    public ResponseEntity<EventResponse> close(@PathVariable("id") int id, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null || user.getRole() != UserRole.BOOKMAKER) return ResponseEntity.status(403).build();

        try {
            Event event = eventService.closeEvent(id);
            return ResponseEntity.ok(toDto(event));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        } catch (EventService.IllegalEventStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    /**
     * Résolution d'un événement. Réservé au rôle BOOKMAKER. Renvoie
     * 400 si le corps de requête est mal formé, 404 si l'événement ou l'issue
     * gagnante n'existe pas, 409 si l'événement n'est pas dans l'état CLOSED.
     */
    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ResolveResultResponse> resolve(
            @PathVariable("id") int id,
            @RequestBody WinningOutcomeRequest body,
            Principal principal
    ) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null || user.getRole() != UserRole.BOOKMAKER) return ResponseEntity.status(403).build();

        if (body == null || body.getWinningOutcomeId() == null) return ResponseEntity.status(400).build();

        try {
            EventService.ResolveOutcome result = eventService.resolveEvent(id, body.getWinningOutcomeId());
            ResolveResultResponse dto = new ResolveResultResponse(
                    result.event().getId(),
                    result.event().getStatus(),
                    WinningOutcomeResponse.from(result.winningOutcome()),
                    result.totalDistributed(),
                    result.event().getResolvedAt()
            );
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        } catch (EventService.IllegalEventStateException e) {
            return ResponseEntity.status(409).build();
        }
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private EventResponse toDto(Event event) {
        BigDecimal totalVolume = eventService.totalVolumeForEvent(event.getId());
        Map<Integer, BigDecimal> outcomeTotals = eventService.outcomeTotalsForEvent(event);
        return EventResponse.from(event, totalVolume, outcomeTotals);
    }

    private EventStatus parseStatus(String status) {
        if (status == null) return null;
        try {
            return EventStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Construit un Pageable à partir des paramètres du contrat (page, size,
     * sort). Le contrat accepte trois valeurs de sort : closeAt,asc /
     * closeAt,desc / createdAt,desc. La propriété Java correspondante pour
     * closeAt est closedAt (voir Event).
     *
     * Renvoie null si l'un des paramètres est invalide, ce qui se traduit par
     * un 400 côté contrôleur.
     */
    private Pageable buildPageable(int page, int size, String sort) {
        if (page < 0) return null;
        if (size < 1 || size > MAX_SIZE) return null;

        Sort sortSpec;
        switch (sort) {
            case "closeAt,asc"     -> sortSpec = Sort.by(Sort.Direction.ASC,  "closedAt");
            case "closeAt,desc"    -> sortSpec = Sort.by(Sort.Direction.DESC, "closedAt");
            case "createdAt,desc"  -> sortSpec = Sort.by(Sort.Direction.DESC, "createdAt");
            default -> { return null; }
        }

        return PageRequest.of(page, size, sortSpec);
    }

}
