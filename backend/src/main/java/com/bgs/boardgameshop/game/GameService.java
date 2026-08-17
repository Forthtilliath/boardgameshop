package com.bgs.boardgameshop.game;

import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** Usage interne (admin, page d'accueil) : liste complète, non paginée. */
    @Transactional(readOnly = true)
    public List<GameResponse> getGames(GameFilter filter) {
        return getGames(filter, Pageable.unpaged()).getContent();
    }

    /** Catalogue public : filtré, trié et paginé côté base de données (voir GameSpecifications). */
    @Transactional(readOnly = true)
    public Page<GameResponse> getGames(GameFilter filter, Pageable pageable) {
        var spec = GameSpecifications.fromFilter(filter);

        // La popularité (nb de ventes) vient d'une agrégation sur OrderLine, pas d'une
        // colonne de Game : pas exprimable simplement dans la même Specification/Sort
        // JPA. Traitée à part : tri en mémoire sur le sous-ensemble déjà filtré par la
        // DB (déjà petit vu les autres filtres), puis pagination manuelle.
        if ("popularity".equals(filter.sort())) {
            return getGamesSortedByPopularity(spec, pageable);
        }

        Sort sort = resolveSort(filter.sort());
        Pageable effectivePageable = pageable.isPaged()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort)
                : (sort.isSorted() ? Pageable.unpaged(sort) : Pageable.unpaged());

        return gameRepository.findAll(spec, effectivePageable).map(GameResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return gameRepository.findDistinctCategories();
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

    private Sort resolveSort(String sortKey) {
        if (sortKey == null) {
            return Sort.unsorted();
        }
        return switch (sortKey) {
            case "price_asc" -> Sort.by(Sort.Order.asc("price"));
            case "price_desc" -> Sort.by(Sort.Order.desc("price"));
            case "newest" -> Sort.by(Sort.Order.desc("releaseDate").nullsLast());
            case "rating" -> Sort.by(Sort.Order.desc("reviewsAverage").nullsLast());
            default -> Sort.unsorted();
        };
    }

    private Page<GameResponse> getGamesSortedByPopularity(Specification<Game> spec, Pageable pageable) {
        Map<Long, Long> quantitySoldByGame = computeQuantitySoldByGame();
        List<GameResponse> sorted = gameRepository.findAll(spec).stream()
                .map(GameResponse::fromEntity)
                .sorted(Comparator.<GameResponse, Long>comparing(
                        g -> quantitySoldByGame.getOrDefault(g.id(), 0L)
                ).reversed())
                .toList();

        if (!pageable.isPaged()) {
            return new PageImpl<>(sorted, Pageable.unpaged(), sorted.size());
        }
        int start = (int) pageable.getOffset();
        if (start >= sorted.size()) {
            return new PageImpl<>(List.of(), pageable, sorted.size());
        }
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        return new PageImpl<>(sorted.subList(start, end), pageable, sorted.size());
    }

    private Map<Long, Long> computeQuantitySoldByGame() {
        return orderLineRepository.findByOrder_StatusIn(PAID_STATUSES).stream()
                .collect(Collectors.groupingBy(OrderLine::getGameId, Collectors.summingLong(OrderLine::getQuantity)));
    }
}
