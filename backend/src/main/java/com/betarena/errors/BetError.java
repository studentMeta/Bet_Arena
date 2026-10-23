package com.betarena.errors;

/**
 * Sert à enrichir le retour de BetService.InvalidBetException pour aider le
 * contrôleur à produire un message d'erreur précis.
 */
public class BetError {

    private boolean isAmountInvalid;

    public boolean isAmountInvalid() {
        return isAmountInvalid;
    }

    public void setAmountInvalid(boolean value) {
        isAmountInvalid = value;
    }

}
