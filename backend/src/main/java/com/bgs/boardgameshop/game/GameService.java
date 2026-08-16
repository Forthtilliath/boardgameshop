package com.bgs.boardgameshop.game;

import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {

    private static final List<OrderStatus> PAID_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);
    private static final int RELATED_GAMES_LIMIT = 8;

    private final GameRepository gameRepository;
    private final OrderLineRepository orderLineRepository;

    public GameService(GameRepository gameRepository, OrderLineRepository orderLineRepository) {
        this.gameRepository = gameRepository;
        this.orderLineRepository = orderLineRepository;
    }

    @Transactional(readOnly = true)
    public List<GameResponse> getGames(GameFilter filter) {
        List<GameResponse> games = gameRepository.findAll().stream()
                .map(GameResponse::fromEntity)
                .filter(g -> matches(g, filter))
                .collect(Collectors.toCollection(ArrayList::new));

        sort(games, filter.sort());
        return games;
    }

    @Transactional(readOnly = true)
    public GameResponse getGame(Long id) {
        return GameResponse.fromEntity(findEntity(id));
    }

    @Transactional(readOnly = true)
    public Game findEntity(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
    }

    /**
     * Jeux suggeres sur la fiche d'un jeu : son jeu de base / ses extensions en priorite,
     * puis meme editeur, puis tags en commun (du plus proche au moins proche), puis meme
     * categorie. Dedoublonne et limite a {@link #RELATED_GAMES_LIMIT}.
     */
    @Transactional(readOnly = true)
    public List<GameResponse> getRelatedGames(Long id) {
        Game game = findEntity(id);
        List<Game> all = gameRepository.findAll();

        Set<Game> related = new LinkedHashSet<>();

        if (game.getBaseGame() != null) {
            related.add(game.getBaseGame());
        }

        Long baseId = game.getBaseGame() != null ? game.getBaseGame().getId() : game.getId();
        all.stream()
                .filter(g -> !g.getId().equals(game.getId()))
                .filter(g -> g.getBaseGame() != null && g.getBaseGame().getId().equals(baseId))
                .forEach(related::add);

        if (game.getPublisher() != null) {
            all.stream()
                    .filter(g -> !g.getId().equals(game.getId()))
                    .filter(g -> game.getPublisher().equals(g.getPublisher()))
                    .forEach(related::add);
        }

        Set<Long> gameTagIds = game.getTags().stream().map(Tag::getId).collect(Collectors.toSet());
        if (!gameTagIds.isEmpty()) {
            all.stream()
                    .filter(g -> !g.getId().equals(game.getId()))
                    .filter(g -> g.getTags().stream().anyMatch(t -> gameTagIds.contains(t.getId())))
                    .sorted(Comparator.<Game>comparingLong(
                            g -> g.getTags().stream().filter(t -> gameTagIds.contains(t.getId())).count()
                    ).reversed())
                    .forEach(related::add);
        }

        if (game.getCategory() != null) {
            all.stream()
                    .filter(g -> !g.getId().equals(game.getId()))
                    .filter(g -> game.getCategory().equals(g.getCategory()))
                    .forEach(related::add);
        }

        return related.stream()
                .limit(RELATED_GAMES_LIMIT)
                .map(GameResponse::fromEntity)
                .toList();
    }

    private boolean matches(GameResponse game, GameFilter filter) {
        if (filter.category() != null && !filter.category().isBlank()
                && !filter.category().equalsIgnoreCase(game.category())) {
            return false;
        }
        if (filter.priceMin() != null && game.finalPrice().compareTo(filter.priceMin()) < 0) {
            return false;
        }
        if (filter.priceMax() != null && game.finalPrice().compareTo(filter.priceMax()) > 0) {
            return false;
        }
        if (filter.players() != null) {
            if (game.minPlayers() != null && filter.players() < game.minPlayers()) {
                return false;
            }
            if (game.maxPlayers() != null && filter.players() > game.maxPlayers()) {
                return false;
            }
        }
        if (filter.maxDuration() != null && game.durationMinutes() != null
                && game.durationMinutes() > filter.maxDuration()) {
            return false;
        }
        if (filter.age() != null && game.minAge() != null && game.minAge() > filter.age()) {
            return false;
        }
        if (filter.tagIds() != null && !filter.tagIds().isEmpty()) {
            boolean hasRequestedTag = game.tags().stream()
                    .anyMatch(tag -> filter.tagIds().contains(tag.id()));
            if (!hasRequestedTag) {
                return false;
            }
        }
        return true;
    }

    private void sort(List<GameResponse> games, String sortKey) {
        if (sortKey == null) {
            return;
        }

        Comparator<GameResponse> comparator = switch (sortKey) {
            case "price_asc" -> Comparator.comparing(GameResponse::finalPrice);
            case "price_desc" -> Comparator.comparing(GameResponse::finalPrice).reversed();
            case "newest" -> Comparator.comparing(
                    GameResponse::releaseDate,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
            case "popularity" -> {
                Map<Long, Long> quantitySoldByGame = computeQuantitySoldByGame();
                yield Comparator.<GameResponse, Long>comparing(
                        g -> quantitySoldByGame.getOrDefault(g.id(), 0L)
                ).reversed();
            }
            case "rating" -> Comparator.comparing(
                    (GameResponse g) -> g.reviewsAverage() == null ? -1.0 : g.reviewsAverage()
            ).reversed();
            default -> null;
        };

        if (comparator != null) {
            games.sort(comparator);
        }
    }

    private Map<Long, Long> computeQuantitySoldByGame() {
        return orderLineRepository.findByOrder_StatusIn(PAID_STATUSES).stream()
                .collect(Collectors.groupingBy(OrderLine::getGameId, Collectors.summingLong(OrderLine::getQuantity)));
    }
}
