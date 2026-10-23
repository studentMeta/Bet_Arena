package com.betarena.enums;

/**
 * Rôle d'un utilisateur. Sert de base à l'autorisation Spring Security : la
 * valeur d'enum est préfixée par ROLE_ pour former l'authority (ROLE_BETTOR ou
 * ROLE_BOOKMAKER).
 */
public enum UserRole {

    /** Parieur standard. C'est le rôle attribué à toute inscription publique. */
    BETTOR,

    /** Administrateur : peut créer, clôturer et résoudre des événements. */
    BOOKMAKER

}
