package com.betarena.dto.response;

/**
 * Réponse de l'endpoint POST /api/auth/login.
 *
 * Le JSON renvoyé a deux champs : token (le JWT signé) et user (un résumé du
 * profil, sans e-mail ni mot de passe).
 */
public record AuthResponse(String token, UserSummaryResponse user) {
}
