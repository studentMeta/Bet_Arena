package com.betarena.services;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.betarena.models.User;
import com.betarena.dao.UserRepository;
import com.betarena.security.AuthUser;

/**
 * Pont entre Spring Security et la table users.
 *
 * Quand le filtre JWT a validé un token, il appelle loadUserByUsername avec
 * le username contenu dans le token. Le service va chercher l'utilisateur en
 * base et le présente à Spring Security sous la forme d'un AuthUser, qui
 * porte l'authority ROLE_BETTOR ou ROLE_BOOKMAKER selon son rôle.
 */
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) throw new UsernameNotFoundException(username + " not found.");

        return new AuthUser(user.get());
    }

}
