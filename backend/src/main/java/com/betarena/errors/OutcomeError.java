package com.betarena.errors;

/**
 * Détail de validation pour OutcomeRequest permet d'indique si l'événement 
 * référencé n'existe pas et/ou si la cote fournie est invalide.
 */
public class OutcomeError {

    public boolean isEventInvalid;
    public boolean isOddsInvalid;

    public boolean isEventInvalid() {
        return isEventInvalid;
    }

    public boolean isOddsInvalid() {
        return isOddsInvalid;
    }

    public void setEventInvalid(boolean value) {
        isEventInvalid = value;
    }

    public void setOddsInvalid(boolean value) {
        isOddsInvalid = value;
    }

}
