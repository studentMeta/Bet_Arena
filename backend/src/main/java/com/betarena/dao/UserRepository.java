package com.betarena.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.betarena.models.User;

/**
 * Accès JPA à la table users.
 *
 * findByUsername et findByEmail sont générées automatiquement par Spring Data
 * à partir de leur nom. Elles servent au login (lookup par e-mail) et au
 * chargement du principal après vérification du JWT (lookup par username).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /** Utilisé par AuthService pour résoudre le principal Spring Security. */
    Optional<User> findByUsername(String username);

    /** Utilisé par le login (l'authentification se fait par e-mail dans le contrat). */
    Optional<User> findByEmail(String email);

    /** Tous les utilisateurs triés par solde décroissant alimente le leaderboard public. */
    @Query("SELECT u FROM users u ORDER BY u.balance DESC")
    List<User> findAllRanked();

}
