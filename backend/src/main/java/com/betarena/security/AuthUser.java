package com.betarena.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.betarena.models.User;

/**
 * Adaptateur entre l'entité User et l'interface UserDetails de Spring Security.
 *
 * L'authority exposée est dérivée du rôle (ROLE_BETTOR ou ROLE_BOOKMAKER),
 * c'est elle qui sert aux règles d'autorisation type hasRole.
 *
 * Les booléens isAccountNonExpired et compagnie sont fixés à true.
 */
public record AuthUser(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /** Accès à l'entité sous-jacente pour les contrôleurs qui en ont besoin. */
    public User getUser() {
        return user;
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}