package com.bgs.boardgameshop.config;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.user.Role;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Peuple le catalogue et quelques comptes de demonstration au demarrage, pour
 * avoir des donnees pretes a l'emploi sans base externe.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(GameRepository gameRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedGames();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.saveAll(java.util.List.of(
                User.builder()
                        .email("admin@bgs.fr")
                        .passwordHash(passwordEncoder.encode("admin1234"))
                        .firstName("Admin")
                        .lastName("BGS")
                        .role(Role.ADMIN)
                        .createdAt(Instant.now())
                        .build(),
                User.builder()
                        .email("user@bgs.fr")
                        .passwordHash(passwordEncoder.encode("user1234"))
                        .firstName("Jean")
                        .lastName("Dupont")
                        .role(Role.USER)
                        .createdAt(Instant.now())
                        .build()
        ));
    }

    private void seedGames() {
        if (gameRepository.count() > 0) {
            return;
        }

        gameRepository.saveAll(java.util.List.of(
                Game.builder()
                        .name("Catane")
                        .description("Colonisez une ile, echangez des ressources et batissez routes et cites.")
                        .price(new BigDecimal("34.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/catane/400/300")
                        .publisher("Kosmos")
                        .minPlayers(3).maxPlayers(4).durationMinutes(90)
                        .stock(25)
                        .build(),
                Game.builder()
                        .name("Carcassonne")
                        .description("Construisez cites, routes et abbayes en posant des tuiles au fil de la partie.")
                        .price(new BigDecimal("24.90"))
                        .category("Famille")
                        .imageUrl("https://picsum.photos/seed/carcassonne/400/300")
                        .publisher("Hans im Gluck")
                        .minPlayers(2).maxPlayers(5).durationMinutes(45)
                        .stock(30)
                        .build(),
                Game.builder()
                        .name("7 Wonders")
                        .description("Developpez une civilisation antique a travers trois ages, en draft de cartes.")
                        .price(new BigDecimal("39.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/7wonders/400/300")
                        .publisher("Repos Production")
                        .minPlayers(3).maxPlayers(7).durationMinutes(30)
                        .stock(18)
                        .build(),
                Game.builder()
                        .name("Terraforming Mars")
                        .description("Dirigez une corporation chargee de rendre Mars habitable.")
                        .price(new BigDecimal("54.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/terraforming/400/300")
                        .publisher("FryxGames")
                        .minPlayers(1).maxPlayers(5).durationMinutes(120)
                        .stock(12)
                        .build(),
                Game.builder()
                        .name("Azul")
                        .description("Composez les plus beaux motifs d'azulejos en piochant des tuiles colorees.")
                        .price(new BigDecimal("29.90"))
                        .category("Ambiance")
                        .imageUrl("https://picsum.photos/seed/azul/400/300")
                        .publisher("Plan B Games")
                        .minPlayers(2).maxPlayers(4).durationMinutes(40)
                        .stock(22)
                        .build(),
                Game.builder()
                        .name("Dixit")
                        .description("Racontez une histoire a partir d'une carte illustree et faites deviner les autres.")
                        .price(new BigDecimal("27.90"))
                        .category("Ambiance")
                        .imageUrl("https://picsum.photos/seed/dixit/400/300")
                        .publisher("Libellud")
                        .minPlayers(3).maxPlayers(6).durationMinutes(30)
                        .stock(20)
                        .build(),
                Game.builder()
                        .name("Pandemic")
                        .description("Unissez vos forces pour enrayer quatre epidemies mondiales avant qu'il ne soit trop tard.")
                        .price(new BigDecimal("32.90"))
                        .category("Cooperatif")
                        .imageUrl("https://picsum.photos/seed/pandemic/400/300")
                        .publisher("Z-Man Games")
                        .minPlayers(2).maxPlayers(4).durationMinutes(45)
                        .stock(15)
                        .build(),
                Game.builder()
                        .name("Splendor")
                        .description("Batissez un empire de marchand de pierres precieuses de la Renaissance.")
                        .price(new BigDecimal("26.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/splendor/400/300")
                        .publisher("Space Cowboys")
                        .minPlayers(2).maxPlayers(4).durationMinutes(30)
                        .stock(28)
                        .build(),
                Game.builder()
                        .name("Codenames")
                        .description("Faites deviner les mots de votre equipe en un seul indice, sans toucher a ceux de l'adversaire.")
                        .price(new BigDecimal("19.90"))
                        .category("Party Game")
                        .imageUrl("https://picsum.photos/seed/codenames/400/300")
                        .publisher("Czech Games Edition")
                        .minPlayers(2).maxPlayers(8).durationMinutes(15)
                        .stock(35)
                        .build(),
                Game.builder()
                        .name("Wingspan")
                        .description("Attirez les plus beaux oiseaux dans vos reserves naturelles.")
                        .price(new BigDecimal("44.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/wingspan/400/300")
                        .publisher("Stonemaier Games")
                        .minPlayers(1).maxPlayers(5).durationMinutes(70)
                        .stock(10)
                        .build()
        ));
    }
}
