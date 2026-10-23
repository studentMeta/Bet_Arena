package com.betarena.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.betarena.dto.request.UserRequest;
import com.betarena.models.User;
import com.betarena.errors.UserError;
import com.betarena.dao.UserRepository;

/**
 * Coeur métier autour de l'entité User : création de comptes, mise à jour
 * du solde, requêtes de lecture, validation des champs d'un UserRequest.
 */
@Service
public class UserService {

    /** Solde de départ offert à chaque nouvel inscrit (1 000 BetCoins). */
    private static final BigDecimal STARTING_BALANCE = new BigDecimal("1000.00");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Levée quand un UserRequest échoue la validation. Le détail des champs
     * en faute est attaché via UserError.
     */
    public static class InvalidUserException extends RuntimeException {

        private final UserError exceptionDetails;

        public InvalidUserException(UserError exceptionDetails) {
            this.exceptionDetails = exceptionDetails;
        }

        public UserError getExceptionDetails() {
            return exceptionDetails;
        }

    }

    /**
     * Crée un nouvel utilisateur après validation : username unique, e-mail
     * unique, mot de passe d'au moins 8 caractères. Le mot de passe est haché
     * en BCrypt avant insertion, et le solde initial est posé à
     * STARTING_BALANCE. Lève InvalidUserException si l'un des champs n'est
     * pas valide.
     */
    public User createUser(UserRequest userRequest) throws InvalidUserException {
        UserError error = validateUser(userRequest);

        if (error != null) throw new InvalidUserException(error);

        User user = new User();

        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(hashPassword(userRequest.getPassword()));
        user.setRole(userRequest.getRole());
        user.setBalance(STARTING_BALANCE);

        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur s'il existe et renvoie son ancien état (ou null
     * si rien n'a été supprimé).
     */
    public User deleteUser(int id) {
        User user = getUserById(id);

        if (user != null) userRepository.deleteById(id);

        return user;
    }

    /**
     * Met à jour le username, l'e-mail et le rôle. Le mot de passe et le solde
     * ne sont volontairement pas modifiés ici.
     *
     * Lève Exception si l'utilisateur n'existe pas, InvalidUserException si
     * les nouvelles valeurs ne passent pas la validation.
     */
    public User updateUser(int id, UserRequest userRequest) throws Exception, InvalidUserException {
        UserError error = validateUser(userRequest);

        if (error != null) throw new InvalidUserException(error);

        User user = getUserById(id);

        if (user == null) throw new Exception("User not found.");

        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRequest.getRole());

        return userRepository.save(user);
    }

    /**
     * Ajoute (ou retire, si amount est négatif) un montant au solde du parieur.
     *
     * C'est l'opération utilisée à la fois pour débiter une mise au moment de
     * placer un pari, et pour créditer un gain lors de la résolution d'un
     * événement. Refuse de descendre en dessous de zéro (lève
     * IllegalArgumentException). Lève NoSuchElementException si l'id ne
     * correspond à aucun utilisateur.
     */
    public User updateUserBalance(int id, BigDecimal amount) {
        User user = getUserById(id);

        if (user == null) throw new NoSuchElementException("User not found.");

        BigDecimal newBalance = user.getBalance().add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer ce pari.");
        }

        user.setBalance(newBalance);

        return userRepository.save(user);
    }

    /** Renvoie l'utilisateur ou null s'il n'existe pas. */
    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    /** Lookup par username, utilisé après validation du JWT. */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /** Lookup par e-mail, utilisé par le login. */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /** Tous les utilisateurs, réservé aux usages admin/debug. */
    public List<User> getAll() {
        return userRepository.findAll();
    }

    /** Utilisateurs triés par solde décroissant, base du classement public. */
    public List<User> getRankedUsers() {
        return userRepository.findAllRanked();
    }

    /** Nombre total d'utilisateurs enregistrés. Exposé via /api/admin/stats. */
    public long getUserCount() {
        return userRepository.count();
    }

    /** Encodage BCrypt délégué au PasswordEncoder défini dans la configuration. */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /** Indique si le username est déjà pris par un compte existant. */
    public boolean isUsernameUsed(String username) {
        return getUserByUsername(username) != null;
    }

    /** Indique si l'e-mail est déjà associé à un compte existant. */
    public boolean isEmailUsed(String email) {
        return getUserByEmail(email) != null;
    }

    /** Format username : 3 à 30 caractères alphanumériques, pas d'accent ni d'espace. */
    public boolean validateUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9]{3,30}$");
    }

    /** Vérification d'e-mail légère via une regex maison. */
    public boolean validateEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validation de mot de passe : au moins 8 caractères.
     */
    public boolean validatePassword(String password) {
        return password != null && password.length() >= 8;
    }

    /**
     * Vérifie tous les champs d'un UserRequest. Renvoie null si tout est OK,
     * sinon un UserError avec un drapeau par champ en faute pour que le
     * frontend puisse afficher des messages précis.
     */
    public UserError validateUser(UserRequest userRequest) {
        boolean validUsername = validateUsername(userRequest.getUsername()) && ! isUsernameUsed(userRequest.getUsername());
        boolean validEmail = validateEmail(userRequest.getEmail()) && ! isEmailUsed(userRequest.getEmail());
        boolean validPassword = validatePassword(userRequest.getPassword());

        if (validUsername && validEmail && validPassword) return null;

        UserError error = new UserError();

        error.setUsernameInvalid(!validUsername);
        error.setEmailInvalid(!validEmail);
        error.setPasswordInvalid(!validPassword);

        return error;
    }

}
