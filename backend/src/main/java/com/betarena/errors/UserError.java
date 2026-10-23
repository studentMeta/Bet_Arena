package com.betarena.errors;

/**
 * Permet au frontend d'afficher une erreur précise à côté du bon input 
 * plutôt qu'un message générique.
 */
public class UserError {

    private boolean isUsernameInvalid;
    private boolean isEmailInvalid;
    private boolean isPasswordInvalid;

    public boolean isUsernameInvalid() {
        return isUsernameInvalid;
    }

    public boolean isEmailInvalid() {
        return isEmailInvalid;
    }

    public boolean isPasswordInvalid() {
        return isPasswordInvalid;
    }

    public void setUsernameInvalid(boolean value) {
        isUsernameInvalid = value;
    }

    public void setEmailInvalid(boolean value) {
        isEmailInvalid = value;
    }

    public void setPasswordInvalid(boolean value) {
        isPasswordInvalid = value;
    }

}
