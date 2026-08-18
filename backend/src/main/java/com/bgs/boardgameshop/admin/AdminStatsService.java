package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminStatsResponse;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.BestOrder;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.DailyOrderCount;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.DailyRevenue;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopSellingGame;
import com.bgs.boardgameshop.order.Order;
import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderRepository;
import com.bgs.boardgameshop.order.OrderStatus;
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
import java.util.stream.Collectors;

/**
 * Seules les commandes ayant réellement été payées (PAYEE, EXPEDIEE, LIVREE)
 * comptent dans le chiffre d'affaires et le classement des ventes.
 */
@Service
public class AdminStatsService {

    private static final List<OrderStatus> PAID_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);
    private static final int TOP_SELLING_LIMIT = 5;
    private static final int DAILY_RANGE_DAYS = 30;

    private final OrderLineRepository orderLineRepository;
    private final OrderRepository orderRepository;

    public AdminStatsService(OrderLineRepository orderLineRepository, OrderRepository orderRepository) {
        this.orderLineRepository = orderLineRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        List<OrderLine> paidLines = orderLineRepository.findByOrder_StatusIn(PAID_STATUSES);

        BigDecimal totalRevenue = paidLines.stream()
                .map(OrderLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orderRepository.countByStatusIn(PAID_STATUSES);
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

        Instant since = Instant.now().minus(DAILY_RANGE_DAYS - 1L, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS);
        List<Order> recentPaidOrders = orderRepository.findByStatusInAndCreatedAtAfter(PAID_STATUSES, since);

        return new AdminStatsResponse(
                totalRevenue,
                totalOrders,
                averageOrderAmount,
                pendingPaymentOrders,
                bestOrder,
                buildOrdersByDay(recentPaidOrders),
                buildRevenueByDay(recentPaidOrders),
                topSellingGames
        );
    }

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
