package com.betarena.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.betarena.dto.response.LeaderboardResponse;
import com.betarena.models.User;
import com.betarena.services.UserService;

/**
 * Endpoints publics autour des utilisateurs. Pour l'instant, uniquement le
 * classement public (GET /api/users/leaderboard, §5.1). Accessible sans JWT
 * il n'expose que des informations volontairement non sensibles.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Classement public des meilleurs parieurs. Le paramètre limit est
     * optionnel : valeur par défaut 10, plafonné à 50. Renvoie 400 si limit
     * est inférieur à 1.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> leaderboard(
            @RequestParam(name = "limit", required = false, defaultValue = "" + DEFAULT_LIMIT) int limit
    ) {
        if (limit < 1) return ResponseEntity.status(400).build();
        int effective = Math.min(limit, MAX_LIMIT);

        List<User> rankedUsers = userService.getRankedUsers();
        int size = Math.min(effective, rankedUsers.size());

        List<LeaderboardResponse> leaderboard = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            User user = rankedUsers.get(i);
            leaderboard.add(new LeaderboardResponse(i + 1, user.getUsername(), user.getBalance()));
        }

        return ResponseEntity.ok(leaderboard);
    }

}
