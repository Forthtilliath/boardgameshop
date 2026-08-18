package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminStatsResponse;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.BestOrder;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.CatalogStats;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.CategoryRevenue;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.DailyOrderCount;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.DailyRevenue;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.DailySignupCount;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.LowStockGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.MostReviewedGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.PromoCodeUsage;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.RatingBucket;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.RestockDemandGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.ReviewStats;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.SalesStats;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.StatusCount;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopCustomer;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopFavoriteGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopRatedGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopSellingGame;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.UserStats;
import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.order.Order;
import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import com.bgs.boardgameshop.promocode.PromoCodeRepository;
import com.bgs.boardgameshop.review.Review;
import com.bgs.boardgameshop.review.ReviewRepository;
import com.bgs.boardgameshop.stockalert.StockAlertRepository;
import com.bgs.boardgameshop.user.User;
import com.bgs.boardgameshop.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Seules les commandes ayant réellement été payées (PAYEE, EXPEDIEE, LIVREE)
 * comptent dans le chiffre d'affaires et les classements liés aux ventes.
 * Le catalogue reste petit (démo) : la plupart des agrégats se font en Java
 * sur des `findAll()` plutôt qu'en requêtes SQL dédiées, dans le style déjà
 * en place ici.
 */
@Service
public class AdminStatsService {

    private static final List<OrderStatus> PAID_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);
    private static final int TOP_SELLING_LIMIT = 5;
    private static final int DAILY_RANGE_DAYS = 30;
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int LOW_STOCK_LIMIT = 8;
    private static final int RANKING_LIMIT = 5;

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final StockAlertRepository stockAlertRepository;
    private final PromoCodeRepository promoCodeRepository;

    public AdminStatsService(
            OrderLineRepository orderLineRepository,
            OrderRepository orderRepository,
            GameRepository gameRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            StockAlertRepository stockAlertRepository,
            PromoCodeRepository promoCodeRepository
    ) {
        this.orderLineRepository = orderLineRepository;
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.stockAlertRepository = stockAlertRepository;
        this.promoCodeRepository = promoCodeRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        List<OrderLine> paidLines = orderLineRepository.findByOrder_StatusIn(PAID_STATUSES);
        List<Order> paidOrders = orderRepository.findByStatusIn(PAID_STATUSES);
        List<Game> games = gameRepository.findAll();

        return new AdminStatsResponse(
                buildSalesStats(paidLines, paidOrders),
                buildCatalogStats(games),
                buildReviewStats(games),
                buildUserStats(paidOrders)
        );
    }

    // ---------------------------------------------------------------- ventes

    private SalesStats buildSalesStats(List<OrderLine> paidLines, List<Order> paidOrders) {
        BigDecimal totalRevenue = paidLines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = paidOrders.size();
        long pendingPaymentOrders = orderRepository.countByStatusIn(List.of(OrderStatus.EN_ATTENTE_PAIEMENT));

        BigDecimal averageOrderAmount = totalOrders == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        BestOrder bestOrder = orderRepository.findFirstByStatusInOrderByTotalAmountDesc(PAID_STATUSES)
                .map(order -> new BestOrder(
                        order.getId(),
                        order.getUser().getFirstName() + " " + order.getUser().getLastName(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .orElse(null);

        Map<Long, List<OrderLine>> linesByGame = paidLines.stream()
                .collect(Collectors.groupingBy(OrderLine::getGameId));

        List<TopSellingGame> topSellingGames = linesByGame.values().stream()
                .map(lines -> new TopSellingGame(
                        lines.get(0).getGameId(),
                        lines.get(0).getGameName(),
                        lines.stream().mapToLong(OrderLine::getQuantity).sum(),
                        lines.stream().map(OrderLine::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                ))
                .sorted(Comparator.comparingLong(TopSellingGame::quantitySold).reversed())
                .limit(TOP_SELLING_LIMIT)
                .toList();

        Instant since = Instant.now().minus(DAILY_RANGE_DAYS - 1L, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        List<Order> recentPaidOrders = orderRepository.findByStatusInAndCreatedAtAfter(PAID_STATUSES, since);

        List<StatusCount> ordersByStatus = orderRepository.findAll().stream()
                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new StatusCount(e.getKey().name(), e.getValue()))
                .sorted(Comparator.comparingLong(StatusCount::count).reversed())
                .toList();

        List<CategoryRevenue> revenueByCategory = buildRevenueByCategory(paidLines);

        List<PromoCodeUsage> promoCodeUsage = promoCodeRepository.findAllByOrderByCodeAsc().stream()
                .map(promo -> new PromoCodeUsage(promo.getCode(), promo.getUsesCount(), promo.getMaxUses(), promo.isActive()))
                .sorted(Comparator.comparingInt(PromoCodeUsage::usesCount).reversed())
                .toList();

        BigDecimal totalDiscountGiven = paidOrders.stream()
                .map(Order::getDiscountAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalesStats(
                totalRevenue, totalOrders, averageOrderAmount, pendingPaymentOrders, bestOrder,
                buildOrdersByDay(recentPaidOrders), buildRevenueByDay(recentPaidOrders),
                topSellingGames, ordersByStatus, revenueByCategory, promoCodeUsage, totalDiscountGiven
        );
    }

    /** Le nom de catégorie n'est pas recopié sur la ligne de commande : on le retrouve via les jeux concernés. */
    private List<CategoryRevenue> buildRevenueByCategory(List<OrderLine> paidLines) {
        Map<Long, String> categoryByGameId = gameRepository
                .findAllById(paidLines.stream().map(OrderLine::getGameId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Game::getId, g -> g.getCategory() == null ? "Autre" : g.getCategory()));

        return paidLines.stream()
                .collect(Collectors.groupingBy(
                        line -> categoryByGameId.getOrDefault(line.getGameId(), "Autre"),
                        Collectors.reducing(BigDecimal.ZERO, OrderLine::getLineTotal, BigDecimal::add)
                ))
                .entrySet().stream()
                .map(e -> new CategoryRevenue(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CategoryRevenue::revenue).reversed())
                .toList();
    }

    // ------------------------------------------------------------- catalogue

    private CatalogStats buildCatalogStats(List<Game> games) {
        BigDecimal totalStockValue = games.stream()
                .map(g -> g.getPrice().multiply(BigDecimal.valueOf(g.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long outOfStockCount = games.stream().filter(g -> g.getStock() == 0).count();
        long lowStockCount = games.stream().filter(g -> g.getStock() <= LOW_STOCK_THRESHOLD).count();

        List<LowStockGame> lowStockGames = games.stream()
                .filter(g -> g.getStock() <= LOW_STOCK_THRESHOLD)
                .sorted(Comparator.comparingInt(Game::getStock))
                .limit(LOW_STOCK_LIMIT)
                .map(g -> new LowStockGame(g.getId(), g.getName(), g.getStock()))
                .toList();

        Instant now = Instant.now();
        long activePromotionsCount = games.stream()
                .filter(g -> g.getDiscountPercent() != null
                        && (g.getDiscountEndsAt() == null || g.getDiscountEndsAt().isAfter(now)))
                .count();

        // Regroupé par id (pas par instance Game : Lombok ne génère pas equals/hashCode dessus).
        List<TopFavoriteGame> topFavoriteGames = userRepository.findAll().stream()
                .flatMap(u -> u.getFavoriteGames().stream())
                .collect(Collectors.groupingBy(Game::getId, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new TopFavoriteGame(e.getKey(), gameNameById(games, e.getKey()), e.getValue()))
                .sorted(Comparator.comparingLong(TopFavoriteGame::favoriteCount).reversed())
                .limit(RANKING_LIMIT)
                .toList();

        List<RestockDemandGame> restockDemand = stockAlertRepository.findAll().stream()
                .filter(alert -> alert.getGame().getStock() == 0)
                .collect(Collectors.groupingBy(alert -> alert.getGame().getId(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new RestockDemandGame(e.getKey(), gameNameById(games, e.getKey()), e.getValue()))
                .sorted(Comparator.comparingLong(RestockDemandGame::alertCount).reversed())
                .limit(RANKING_LIMIT)
                .toList();

        return new CatalogStats(
                totalStockValue, outOfStockCount, lowStockCount,
                lowStockGames, activePromotionsCount, topFavoriteGames, restockDemand
        );
    }

    private String gameNameById(List<Game> games, Long gameId) {
        return games.stream()
                .filter(g -> g.getId().equals(gameId))
                .findFirst()
                .map(Game::getName)
                .orElse("Jeu supprimé");
    }

    // ----------------------------------------------------------------- avis

    private ReviewStats buildReviewStats(List<Game> games) {
        List<Review> allReviews = reviewRepository.findAll();

        Double averageRating = allReviews.isEmpty()
                ? null
                : allReviews.stream().mapToInt(Review::getRating).average().orElse(0);

        Map<Integer, Long> countByStars = allReviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
        List<RatingBucket> ratingDistribution = IntStream.rangeClosed(1, 5)
                .mapToObj(stars -> new RatingBucket(stars, countByStars.getOrDefault(stars, 0L)))
                .toList();

        List<Game> reviewedGames = games.stream().filter(g -> g.getReviewsCount() > 0).toList();

        List<TopRatedGame> topRatedGames = reviewedGames.stream()
                .sorted(Comparator.comparingDouble(Game::getReviewsAverage).reversed())
                .limit(RANKING_LIMIT)
                .map(g -> new TopRatedGame(g.getId(), g.getName(), g.getReviewsAverage(), g.getReviewsCount()))
                .toList();

        List<TopRatedGame> worstRatedGames = reviewedGames.stream()
                .sorted(Comparator.comparingDouble(Game::getReviewsAverage))
                .limit(RANKING_LIMIT)
                .map(g -> new TopRatedGame(g.getId(), g.getName(), g.getReviewsAverage(), g.getReviewsCount()))
                .toList();

        List<MostReviewedGame> mostReviewedGames = reviewedGames.stream()
                .sorted(Comparator.comparingInt(Game::getReviewsCount).reversed())
                .limit(RANKING_LIMIT)
                .map(g -> new MostReviewedGame(g.getId(), g.getName(), g.getReviewsCount()))
                .toList();

        return new ReviewStats(
                averageRating, allReviews.size(), ratingDistribution, topRatedGames, worstRatedGames, mostReviewedGames
        );
    }

    // ------------------------------------------------------------ utilisateurs

    private UserStats buildUserStats(List<Order> paidOrders) {
        long totalUsers = userRepository.count();

        Instant since = Instant.now().minus(DAILY_RANGE_DAYS - 1L, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        List<User> recentUsers = userRepository.findByCreatedAtAfter(since);
        List<DailySignupCount> signupsByDay = buildSignupsByDay(recentUsers);

        Map<User, List<Order>> ordersByCustomer = paidOrders.stream().collect(Collectors.groupingBy(Order::getUser));
        List<TopCustomer> topCustomers = ordersByCustomer.entrySet().stream()
                .map(e -> new TopCustomer(
                        e.getKey().getId(),
                        e.getKey().getFirstName() + " " + e.getKey().getLastName(),
                        e.getValue().stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()
                ))
                .sorted(Comparator.comparing(TopCustomer::totalSpent).reversed())
                .limit(RANKING_LIMIT)
                .toList();

        long payingCustomers = ordersByCustomer.keySet().size();
        double conversionRate = totalUsers == 0 ? 0 : (payingCustomers * 100.0) / totalUsers;

        return new UserStats(totalUsers, signupsByDay, topCustomers, conversionRate);
    }

    // --------------------------------------------------------------- séries journalières

    /** Un point par jour sur les {@value #DAILY_RANGE_DAYS} derniers jours, à zéro si aucune commande ce jour-là. */
    private List<DailyOrderCount> buildOrdersByDay(List<Order> orders) {
        Map<LocalDate, Long> countByDate = orders.stream()
                .collect(Collectors.groupingBy(this::toLocalDate, Collectors.counting()));

        List<DailyOrderCount> result = new ArrayList<>();
        for (LocalDate date : lastNDays()) {
            result.add(new DailyOrderCount(date, countByDate.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<DailyRevenue> buildRevenueByDay(List<Order> orders) {
        Map<LocalDate, BigDecimal> revenueByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        this::toLocalDate,
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalAmount, BigDecimal::add)
                ));

        List<DailyRevenue> result = new ArrayList<>();
        for (LocalDate date : lastNDays()) {
            result.add(new DailyRevenue(date, revenueByDate.getOrDefault(date, BigDecimal.ZERO)));
        }
        return result;
    }

    private List<DailySignupCount> buildSignupsByDay(List<User> users) {
        Map<LocalDate, Long> countByDate = users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate(),
                        Collectors.counting()
                ));

        List<DailySignupCount> result = new ArrayList<>();
        for (LocalDate date : lastNDays()) {
            result.add(new DailySignupCount(date, countByDate.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<LocalDate> lastNDays() {
        LocalDate today = LocalDate.now();
        List<LocalDate> days = new ArrayList<>();
        for (int i = DAILY_RANGE_DAYS - 1; i >= 0; i--) {
            days.add(today.minusDays(i));
        }
        return days;
    }

    private LocalDate toLocalDate(Order order) {
        return order.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
