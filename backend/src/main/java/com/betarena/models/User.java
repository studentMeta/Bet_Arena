package com.betarena.models;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import tools.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.betarena.enums.UserRole;
import com.betarena.serialization.InstantConverter;

/**
 * Compte utilisateur de la plateforme : parieur ou bookmaker.
 *
 * Quelques règles à garder en tête :
 *  - tout nouveau compte démarre à 1 000 BetCoins (constante STARTING_BALANCE côté UserService)
 *  - le mot de passe stocké est un hash BCrypt, jamais le mot de passe en clair
 *  - le rôle est ce qui détermine l'accès aux routes admin.
 */
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    /** Hash BCrypt, exclu du JSON pour ne pas le renvoyer aux clients. */
    @JsonIgnore
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @JsonSerialize(converter = InstantConverter.class)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public int getId() {
        return id;
    }

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

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
