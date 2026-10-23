package com.betarena.errors;

/**
 * Sert à enrichir le retour de EventService.InvalidEventException pour aider le
 * contrôleur à produire un message d'erreur précis.
 */
public class EventError {

    private boolean isTitleInvalid;
    private boolean isClosureTimeInvalid;

    public boolean isTitleInvalid() {
        return isTitleInvalid;
    }

    public boolean isClosureTimeInvalid() {
        return isClosureTimeInvalid;
    }

    public void setTitleInvalid(boolean value) {
        isTitleInvalid = value;
    }

    public void setClosureTimeInvalid(boolean value) {
        isClosureTimeInvalid = value;
    }

}
