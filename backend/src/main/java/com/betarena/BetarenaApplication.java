package com.betarena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entrée Spring Boot. C'est ici que la JVM démarre l'application.
 * Toute la configuration vit dans config/ et security/, et chaque feature
 * est rattachée à son propre package.
 */
@SpringBootApplication
public class BetarenaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BetarenaApplication.class, args);
	}

}
