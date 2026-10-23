package com.betarena.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.annotation.JsonSerialize;

import com.betarena.models.User;
import com.betarena.enums.UserRole;
import com.betarena.serialization.InstantConverter;

/**
 * Réponse de l'endpoint POST /api/auth/register.
 *
 * Le JSON : id, username, email, role, balance, le JWT signé pour
 * cet utilisateur, et createdAt. C'est la seule réponse où on expose à la
 * fois l'e-mail et le token, toutes les autres routes filtrent l'e-mail.
 */
public record RegisterResponse(
        int id,
        String username,
        String email,
        UserRole role,
        BigDecimal balance,
        String token,
        @JsonSerialize(converter = InstantConverter.class) Instant createdAt
) {

    /** Petit helper pour construire la réponse à partir de l'utilisateur persisté et du JWT généré. */
    public static RegisterResponse from(User user, String token) {
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getBalance(),
                token,
                user.getCreatedAt()
        );
    }

}
