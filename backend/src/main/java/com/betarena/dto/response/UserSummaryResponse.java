package com.betarena.dto.response;

import java.math.BigDecimal;

import com.betarena.models.User;
import com.betarena.enums.UserRole;

/**
 * Vue compacte d'un utilisateur destinée aux réponses d'authentification.
 * N'expose ni le mot de passe ni l'adresse e-mail, juste de quoi alimenter
 * l'interface une fois la connexion réussie.
 */
public record UserSummaryResponse(
        int id,
        String username,
        UserRole role,
        BigDecimal balance
) {

    /** Petit helper pour construire la vue à partir de l'entité. */
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getRole(), user.getBalance());
    }

}
