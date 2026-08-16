package com.bgs.boardgameshop.config;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.game.Tag;
import com.bgs.boardgameshop.game.TagRepository;
import com.bgs.boardgameshop.order.Order;
import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import com.bgs.boardgameshop.review.Review;
import com.bgs.boardgameshop.review.ReviewRepository;
import com.bgs.boardgameshop.user.Role;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Peuple le catalogue, quelques tags et comptes de demonstration au demarrage,
 * pour avoir des donnees pretes a l'emploi sans base externe.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final GameRepository gameRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            GameRepository gameRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            ReviewRepository reviewRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.gameRepository = gameRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        Map<String, Tag> tags = seedTags();
        seedGames(tags);
        seedOrderAndReviews();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.saveAll(List.of(
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

    private Map<String, Tag> seedTags() {
        if (tagRepository.count() > 0) {
            return tagRepository.findAll().stream()
                    .collect(Collectors.toMap(Tag::getName, t -> t));
        }

        List<Tag> tags = List.of(
                Tag.builder().name("Cooperatif").slug("cooperatif").build(),
                Tag.builder().name("Gestion de ressources").slug("gestion-de-ressources").build(),
                Tag.builder().name("Deck-building").slug("deck-building").build(),
                Tag.builder().name("Draft").slug("draft").build(),
                Tag.builder().name("Bluff").slug("bluff").build(),
                Tag.builder().name("Enigme").slug("enigme").build(),
                Tag.builder().name("Pose de tuiles").slug("pose-de-tuiles").build(),
                Tag.builder().name("Ambiance familiale").slug("ambiance-familiale").build()
        );

        return tagRepository.saveAll(tags).stream().collect(Collectors.toMap(Tag::getName, t -> t));
    }

    private void seedGames(Map<String, Tag> tags) {
        if (gameRepository.count() > 0) {
            return;
        }

        LocalDate today = LocalDate.now();

        gameRepository.saveAll(List.of(
                Game.builder()
                        .name("Catane")
                        .description("Colonisez une ile, echangez des ressources et batissez routes et cites.")
                        .price(new BigDecimal("34.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/catane/400/300")
                        .publisher("Kosmos")
                        .minPlayers(3).maxPlayers(4).durationMinutes(90)
                        .stock(25)
                        .minAge(10)
                        .releaseDate(today.minusYears(5))
                        .tags(Set.of(tags.get("Gestion de ressources")))
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
                        .minAge(7)
                        .releaseDate(today.minusYears(8))
                        .discountPercent(15)
                        .discountEndsAt(Instant.now().plus(14, ChronoUnit.DAYS))
                        .tags(Set.of(tags.get("Pose de tuiles"), tags.get("Ambiance familiale")))
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
                        .minAge(10)
                        .releaseDate(today.minusYears(6))
                        .tags(Set.of(tags.get("Draft")))
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
                        .minAge(12)
                        .releaseDate(today.minusYears(4))
                        .tags(Set.of(tags.get("Gestion de ressources")))
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
                        .minAge(8)
                        .releaseDate(today.minusYears(3))
                        .tags(Set.of(tags.get("Pose de tuiles")))
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
                        .minAge(6)
                        .releaseDate(today.minusYears(10))
                        .tags(Set.of(tags.get("Ambiance familiale")))
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
                        .minAge(10)
                        .releaseDate(today.minusYears(9))
                        .discountPercent(20)
                        .discountEndsAt(Instant.now().plus(7, ChronoUnit.DAYS))
                        .tags(Set.of(tags.get("Cooperatif")))
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
                        .minAge(8)
                        .releaseDate(today.minusYears(7))
                        .tags(Set.of(tags.get("Gestion de ressources")))
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
                        .minAge(14)
                        .releaseDate(today.minusYears(6))
                        .tags(Set.of(tags.get("Bluff")))
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
                        .minAge(10)
                        .releaseDate(today.minusYears(2))
                        .tags(Set.of(tags.get("Gestion de ressources"), tags.get("Deck-building")))
                        .build(),
                Game.builder()
                        .name("Nemesis")
                        .description("Survivez a bord d'un vaisseau infeste de creatures hostiles. Precommande ouverte.")
                        .price(new BigDecimal("89.90"))
                        .category("Cooperatif")
                        .imageUrl("https://picsum.photos/seed/nemesis/400/300")
                        .publisher("Awaken Realms")
                        .minPlayers(1).maxPlayers(5).durationMinutes(120)
                        .stock(8)
                        .minAge(16)
                        .releaseDate(today.plusMonths(2))
                        .tags(Set.of(tags.get("Cooperatif"), tags.get("Bluff")))
                        .build(),
                Game.builder()
                        .name("Cascadia")
                        .description("Amenagez un paysage naturel harmonieux pour attirer une faune variee. Sortie a venir.")
                        .price(new BigDecimal("36.90"))
                        .category("Strategie")
                        .imageUrl("https://picsum.photos/seed/cascadia/400/300")
                        .publisher("Flatout Games")
                        .minPlayers(1).maxPlayers(4).durationMinutes(45)
                        .stock(0)
                        .minAge(10)
                        .releaseDate(today.plusMonths(1))
                        .tags(Set.of(tags.get("Pose de tuiles")))
                        .build()
        ));
    }

    /**
     * Une commande livree + quelques avis, pour que la page d'accueil (avis
     * recents, top ventes) ne soit pas vide au premier demarrage.
     */
    private void seedOrderAndReviews() {
        if (orderRepository.count() > 0) {
            return;
        }

        User buyer = userRepository.findByEmail("user@bgs.fr")
                .orElseThrow(() -> new IllegalStateException("Utilisateur de demo introuvable"));
        List<Game> games = gameRepository.findAll();
        Game catane = findByName(games, "Catane");
        Game pandemic = findByName(games, "Pandemic");
        Game azul = findByName(games, "Azul");

        Order order = Order.builder()
                .user(buyer)
                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .status(OrderStatus.LIVREE)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (Game game : List.of(catane, pandemic, azul)) {
            OrderLine line = OrderLine.builder()
                    .gameId(game.getId())
                    .gameName(game.getName())
                    .unitPrice(game.getPrice())
                    .quantity(1)
                    .build();
            order.addLine(line);
            total = total.add(line.getLineTotal());
        }
        order.setTotalAmount(total);
        orderRepository.save(order);

        reviewRepository.saveAll(List.of(
                Review.builder()
                        .game(catane).user(buyer).rating(5)
                        .comment("Un classique indemodable, parfait pour recevoir en famille !")
                        .createdAt(Instant.now().minus(4, ChronoUnit.DAYS))
                        .build(),
                Review.builder()
                        .game(pandemic).user(buyer).rating(4)
                        .comment("Tres bon jeu cooperatif, tendu jusqu'a la derniere carte.")
                        .createdAt(Instant.now().minus(3, ChronoUnit.DAYS))
                        .build(),
                Review.builder()
                        .game(azul).user(buyer).rating(5)
                        .comment("Magnifique et addictif, on ne s'en lasse pas.")
                        .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                        .build()
        ));

        for (Game game : List.of(catane, pandemic, azul)) {
            List<Review> gameReviews = reviewRepository.findByGame_IdOrderByCreatedAtDesc(game.getId());
            double average = gameReviews.stream().mapToInt(Review::getRating).average().orElse(0);
            game.setReviewsAverage(average);
            game.setReviewsCount(gameReviews.size());
            gameRepository.save(game);
        }
    }

    private Game findByName(List<Game> games, String name) {
        return games.stream()
                .filter(game -> game.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Jeu introuvable dans le seed : " + name));
    }
}
