package com.bgs.boardgameshop.order;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.order.dto.CreateOrderRequest;
import com.bgs.boardgameshop.order.dto.OrderItemRequest;
import com.bgs.boardgameshop.order.dto.OrderResponse;
import com.bgs.boardgameshop.user.Role;
import com.bgs.boardgameshop.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;

    public OrderService(OrderRepository orderRepository, GameRepository gameRepository) {
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Cree une commande a partir du panier envoye par le client, decremente le
     * stock des jeux commandes et retourne la confirmation. Le site etant
     * fictif, aucun paiement reel n'est effectue : la commande est confirmee
     * immediatement.
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User currentUser) {
        Order order = Order.builder()
                .user(currentUser)
                .createdAt(Instant.now())
                .status(OrderStatus.CONFIRMEE)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest item : request.items()) {
            Game game = gameRepository.findById(item.gameId())
                    .orElseThrow(() -> new GameNotFoundException(item.gameId()));

            if (game.getStock() < item.quantity()) {
                throw new InsufficientStockException(game.getName(), item.quantity(), game.getStock());
            }

            game.setStock(game.getStock() - item.quantity());
            gameRepository.save(game);

            OrderLine line = OrderLine.builder()
                    .gameId(game.getId())
                    .gameName(game.getName())
                    .unitPrice(game.getPrice())
                    .quantity(item.quantity())
                    .build();
            order.addLine(line);

            total = total.add(line.getLineTotal());
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, User currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Cette commande ne vous appartient pas");
        }

        return OrderResponse.fromEntity(order);
    }
}
