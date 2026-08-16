package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminStatsResponse;
import com.bgs.boardgameshop.admin.dto.AdminStatsResponse.TopSellingGame;
import com.bgs.boardgameshop.order.OrderLine;
import com.bgs.boardgameshop.order.OrderLineRepository;
import com.bgs.boardgameshop.order.OrderRepository;
import com.bgs.boardgameshop.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Seules les commandes ayant reellement ete payees (PAYEE, EXPEDIEE, LIVREE)
 * comptent dans le chiffre d'affaires et le classement des ventes.
 */
@Service
public class AdminStatsService {

    private static final List<OrderStatus> PAID_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);
    private static final int TOP_SELLING_LIMIT = 5;

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

        return new AdminStatsResponse(totalRevenue, totalOrders, topSellingGames);
    }
}
