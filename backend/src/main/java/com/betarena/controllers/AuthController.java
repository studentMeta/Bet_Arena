package com.betarena.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.betarena.dto.request.UserRequest;
import com.betarena.dto.response.AuthResponse;
import com.betarena.dto.response.RegisterResponse;
import com.betarena.dto.response.UserSummaryResponse;
import com.betarena.models.User;
import com.betarena.security.JwtService;
import com.betarena.services.UserService;

/**
 * Endpoints d'authentification : inscription, connexion et
 * lecture du profil courant.
 *
 * L'inscription et la connexion sont en accès libre (whitelistées dans
 * SecurityConfig). /me exige un JWT valide, le username est extrait du token
 * par le filtre JWT puis injecté ici via Principal.
 */
@RestController
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Inscrit un nouvel utilisateur et renvoie immédiatement un JWT, pour
     * éviter au client d'enchaîner avec un appel à login juste après.
     */
    @PostMapping("/api/auth/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody UserRequest userRequest) {
        try {
            User user = userService.createUser(userRequest);
            String token = jwtService.generateToken(user.getUsername());

            return ResponseEntity.status(201).body(RegisterResponse.from(user, token));
        } catch (UserService.InvalidUserException e) {
            return ResponseEntity.status(400).build();
        }
    }

    /**
     * Authentifie l'utilisateur par e-mail et mot de passe, puis renvoie un
     * JWT accompagné d'un résumé du profil. Renvoie 401 si l'e-mail
     * n'existe pas ou si le mot de passe ne correspond pas.
     */
    @PostMapping("/api/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserRequest userRequest) {
        User user = userService.getUserByEmail(userRequest.getEmail());

        if (user == null || ! passwordEncoder.matches(userRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtService.generateToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(token, UserSummaryResponse.from(user)));
    }

    /**
     * Renvoie le profil complet de l'utilisateur authentifié, e-mail compris.
     * Utilisé par le frontend au démarrage pour restaurer la session
     * à partir du token stocké en local.
     */
    @GetMapping("/api/auth/me")
    public ResponseEntity<User> getAuthenticatedUser(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());

        if (user == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(user);
    }

}
