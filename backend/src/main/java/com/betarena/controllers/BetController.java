package com.betarena.controllers;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.betarena.dto.request.BetRequest;
import com.betarena.dto.response.BetResponse;
import com.betarena.dto.response.PageResponse;
import com.betarena.models.Bet;
import com.betarena.models.Event;
import com.betarena.models.Outcome;
import com.betarena.models.User;
import com.betarena.enums.BetStatus;
import com.betarena.enums.EventStatus;
import com.betarena.enums.UserRole;
import com.betarena.services.BetService;
import com.betarena.services.OutcomeService;
import com.betarena.services.UserService;

/**
 * Endpoints de paris : placement d'un pari et historique
 * paginé du parieur courant.
 *
 * Le placement (POST /api/bets) est encapsulé dans une transaction : on
 * débite le solde puis on insère le pari. Si la création échoue à n'importe
 * quel moment, le débit est annulé automatiquement par le rollback Spring.
 */
@RestController
@RequestMapping("/api/bets")
public class BetController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final BetService betService;
    private final OutcomeService outcomeService;
    private final UserService userService;

    public BetController(BetService betService, OutcomeService outcomeService, UserService userService) {
        this.betService = betService;
        this.outcomeService = outcomeService;
        this.userService = userService;
    }

    /**
     * Place un pari. Réservé au rôle BETTOR. Renvoie 400 si le montant
     * est invalide ou le solde insuffisant, 404 si l'issue n'existe pas, 409
     * si l'événement n'est plus ouvert, 422 si la validation métier échoue.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<BetResponse> place(@RequestBody BetRequest betRequest, Principal principal) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null || user.getRole() != UserRole.BETTOR) return ResponseEntity.status(403).build();

        Outcome outcome = outcomeService.getOutcomeById(betRequest.getOutcomeId());
        if (outcome == null) return ResponseEntity.status(404).build();

        Event event = outcome.getEvent();
        if (event.getStatus() != EventStatus.OPEN) return ResponseEntity.status(409).build();

        if (betRequest.getAmount() == null || betRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.status(400).build();
        }

        try {
            User updated = userService.updateUserBalance(user.getId(), betRequest.getAmount().negate());
            Bet bet = betService.createBet(updated.getId(), betRequest);

            return ResponseEntity.status(201).body(BetResponse.from(bet, updated.getBalance()));
        } catch (IllegalArgumentException e) {
            // Solde insuffisant
            return ResponseEntity.status(400).build();
        } catch (BetService.InvalidBetException e) {
            return ResponseEntity.status(422).build();
        }
    }

    /**
     * Historique paginé des paris de l'utilisateur authentifié. Trois
     * query params optionnels : status (PENDING, WON, LOST), page, size.
     * Tri implicite par date décroissante (plus récent en premier).
     */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<BetResponse>> myBets(
            Principal principal,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "size", required = false, defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        User user = userService.getUserByUsername(principal.getName());
        if (user == null || user.getRole() != UserRole.BETTOR) return ResponseEntity.status(403).build();

        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) return ResponseEntity.status(400).build();

        BetStatus betStatus = parseStatus(status);
        if (status != null && betStatus == null) return ResponseEntity.status(400).build();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "placedAt"));

        Page<Bet> bets = betStatus != null
                ? betService.findByBettorAndStatus(user.getId(), betStatus, pageable)
                : betService.findByBettor(user.getId(), pageable);

        return ResponseEntity.ok(PageResponse.from(bets, BetResponse::from));
    }

    private BetStatus parseStatus(String status) {
        if (status == null) return null;
        try {
            return BetStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}
