package com.betarena.controllers;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.betarena.dto.response.EventResponse;
import com.betarena.dto.response.PageResponse;
import com.betarena.models.Event;
import com.betarena.enums.EventStatus;
import com.betarena.services.BetService;
import com.betarena.services.EventService;
import com.betarena.services.UserService;

/**
 * Endpoints d'administration : liste paginée des événements pour la
 * console d'admin et statistiques globales de la plateforme.
 *
 * L'autorisation ROLE_BOOKMAKER est gérée en amont par SecurityConfig sur le
 * pattern /api/admin/**, donc on n'a pas besoin de revérifier le rôle ici.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final BetService betService;
    private final EventService eventService;
    private final UserService userService;

    public AdminController(BetService betService, EventService eventService, UserService userService) {
        this.betService = betService;
        this.eventService = eventService;
        this.userService = userService;
    }

    /**
     * Liste paginée des événements pour la console d'administration.
     *
     * Contrairement à GET /api/events grand public, aucun filtre par défaut
     * n'est appliqué sur le statut : l'admin voit tout, ouverts comme résolus,
     * sauf s'il choisit lui-même de filtrer.
     */
    @GetMapping("/events")
    public ResponseEntity<PageResponse<EventResponse>> events(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) return ResponseEntity.status(400).build();

        EventStatus eventStatus = null;
        if (status != null) {
            try {
                eventStatus = EventStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(400).build();
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Event> events = eventService.listEvents(eventStatus, category, pageable);
        return ResponseEntity.ok(PageResponse.from(events, this::toDto));
    }

    /**
     * Statistiques globales de la plateforme : nombre d'utilisateurs,
     * d'événements (par statut), de paris placés, volume total misé et total
     * distribué en gains.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        long totalUsers = userService.getUserCount();
        long totalEvents = eventService.getEventCount();
        long openEvents = eventService.getEventCountByStatus(EventStatus.OPEN);
        long resolvedEvents = eventService.getEventCountByStatus(EventStatus.RESOLVED);
        BigDecimal totalBetsVolume = nullToZero(betService.getTotalAmount());
        long totalBetsCount = betService.getBetCount();
        BigDecimal totalDistributed = nullToZero(betService.getTotalDistributed());

        Map<String, Object> body = Map.of(
                "totalUsers", totalUsers,
                "totalEvents", totalEvents,
                "openEvents", openEvents,
                "resolvedEvents", resolvedEvents,
                "totalBetsVolume", totalBetsVolume,
                "totalBetsCount", totalBetsCount,
                "totalDistributed", totalDistributed
        );

        return ResponseEntity.ok(body);
    }

    private EventResponse toDto(Event event) {
        BigDecimal totalVolume = eventService.totalVolumeForEvent(event.getId());
        Map<Integer, BigDecimal> outcomeTotals = eventService.outcomeTotalsForEvent(event);
        return EventResponse.from(event, totalVolume, outcomeTotals);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

}
