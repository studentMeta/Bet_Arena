package com.betarena.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Génération et validation des JSON Web Tokens.
 *
 * Les tokens sont signés HMAC-SHA256 avec la clé symétrique fournie via la
 * propriété jwt.secret. Le sujet (claim sub) contient le username de
 * l'utilisateur, ce qui permet à JwtAuthenticationFilter d'aller chercher les
 * détails de l'utilisateur via UserDetailsService sans embarquer
 * d'informations sensibles dans le token.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Génère un JWT signé pour l'utilisateur dont le username est passé en
     * paramètre. La durée de validité vient de jwt.expiration-ms.
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extrait le username (claim sub) contenu dans un JWT valide. Lève une
     * JwtException si la signature ne tient pas ou si le token est expiré.
     */
    public String extractUsername(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);

        return jws.getPayload().getSubject();
    }

    /** Renvoie true si la signature est correcte et le token non expiré. */
    public boolean isTokenValid(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
