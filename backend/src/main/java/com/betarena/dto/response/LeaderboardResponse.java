package com.betarena.dto.response;

import java.math.BigDecimal;

/**
 * Une ligne du classement public renvoyée par GET /api/users/leaderboard.
 *
 * Volontairement réduit à trois champs publics, rank, username et balance.
 * On n'expose ni l'e-mail ni l'identifiant interne.
 */
public class LeaderboardResponse {

    final private int rank;
    final private String username;
    final private BigDecimal balance;

    public LeaderboardResponse(int rank, String username, BigDecimal balance) {
        this.balance = balance;
        this.username = username;
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getUsername() {
        return username;
    }

}
