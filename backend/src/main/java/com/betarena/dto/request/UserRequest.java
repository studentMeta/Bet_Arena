package com.betarena.dto.request;

import jakarta.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.betarena.enums.UserRole;

/**
 * Corps de requête utilisé à la fois pour l'inscription (POST /auth/register)
 * et pour la connexion (POST /auth/login).
 *
 * Côté inscription, on attend les trois champs username, email et password.
 * Côté connexion, seuls email et password servent, le username est ignoré.
 *
 * Le mot de passe est toujours envoyé en clair sur HTTPS puis haché côté
 * serveur (BCrypt) par UserService avant insertion en base.
 */
public class UserRequest {

    private String username;

    @Email(message = "Le format de l'adresse e-mail est invalide")
    @JsonProperty(required = true)
    private String email;

    @JsonProperty(required = true)
    private String password;

    /** Par défaut tout nouvel inscrit est un parieur, un BOOKMAKER est créé hors API publique. */
    private UserRole role = UserRole.BETTOR;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

}
