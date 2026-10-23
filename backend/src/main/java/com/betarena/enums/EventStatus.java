package com.betarena.enums;

/**
 * Cycle de vie d'un événement : OPEN, puis CLOSED, puis RESOLVED. Les transitions
 * sont à sens unique, on ne revient jamais en arrière.
 */
public enum EventStatus {

    /** Visible dans le lobby et accepte les paris tant que la date limite n'est pas passée. */
    OPEN,

    /** Date limite atteinte (ou clôture manuelle) : plus de pari possible, en attente du verdict. */
    CLOSED,

    /** Résultat connu : l'issue gagnante est désignée et les gains ont été distribués. */
    RESOLVED

}
