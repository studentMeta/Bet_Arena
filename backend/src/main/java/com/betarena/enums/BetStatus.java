package com.betarena.enums;

/**
 * État d'un pari. À la création il est PENDING ; la résolution de l'événement
 * le bascule définitivement sur WON ou LOST.
 */
public enum BetStatus {

    /** En attente du verdict la mise a déjà été débitée du solde du parieur. */
    PENDING,

    /** Le pari est gagnant : la mise multipliée par la cote a été créditée sur le compte du parieur. */
    WON,

    /** Le pari est perdant : la mise est définitivement perdue. */
    LOST

}
