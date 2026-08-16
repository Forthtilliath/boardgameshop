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
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;

    public OrderService(OrderRepository orderRepository, GameRepository gameRepository) {
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Cree une commande a partir du panier envoye par le client et decremente
     * (reserve) le stock des jeux commandes. La commande reste EN_ATTENTE_PAIEMENT
     * jusqu'a confirmation du paiement Stripe (voir markAsPaid/markAsFailed).
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User currentUser) {
        Order order = Order.builder()
                .user(currentUser)
                .createdAt(Instant.now())
                .status(OrderStatus.EN_ATTENTE_PAIEMENT)
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
        return OrderResponse.fromEntity(getOwnedOrder(id, currentUser));
    }

    /**
     * Recupere la commande si elle appartient a l'utilisateur (ou s'il est admin),
     * pour usage interne (ex : PaymentService avant de creer un Payment Intent).
     */
    @Transactional(readOnly = true)
    public Order getOwnedOrder(Long id, User currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        boolean isOwner = order.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Cette commande ne vous appartient pas");
        }

        return order;
    }

    @Transactional
    public void attachPaymentIntent(Order order, String paymentIntentId) {
        order.setStripePaymentIntentId(paymentIntentId);
        orderRepository.save(order);
    }

    @Transactional
    public void markAsPaid(String paymentIntentId) {
        Order order = findByPaymentIntentId(paymentIntentId);
        order.setStatus(OrderStatus.PAYEE);
        orderRepository.save(order);
    }

    /**
     * Le paiement a echoue : la commande est marquee ECHOUEE et le stock
     * reserve a la creation de la commande est restitue.
     */
    @Transactional
    public void markAsFailed(String paymentIntentId) {
        Order order = findByPaymentIntentId(paymentIntentId);
        order.setStatus(OrderStatus.ECHOUEE);
        restituteStock(order);
        orderRepository.save(order);
    }

    /**
     * Liste des commandes pour le dashboard admin (toutes, ou filtrees par
     * statut), les plus recentes en premier.
     */
    @Transactional(readOnly = true)
    public List<Order> adminListOrders(OrderStatus statusFilter) {
        return statusFilter == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(statusFilter);
    }

    /**
     * Fait progresser une commande dans son cycle de vie (PAYEE -> EXPEDIEE ->
     * LIVREE, ou annulation). Restitue le stock si la commande est annulee
     * apres avoir ete payee.
     */
    @Transactional
    public Order adminUpdateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        validateTransition(order.getStatus(), newStatus);

        if (newStatus == OrderStatus.ANNULEE) {
            restituteStock(order);
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    private void validateTransition(OrderStatus from, OrderStatus to) {
        boolean valid = switch (from) {
            case PAYEE -> to == OrderStatus.EXPEDIEE || to == OrderStatus.ANNULEE;
            case EXPEDIEE -> to == OrderStatus.LIVREE || to == OrderStatus.ANNULEE;
            default -> false;
        };
        if (!valid) {
            throw new InvalidOrderStatusTransitionException(from, to);
        }
    }

    private void restituteStock(Order order) {
        for (OrderLine line : order.getLines()) {
            gameRepository.findById(line.getGameId()).ifPresent(game -> {
                game.setStock(game.getStock() + line.getQuantity());
                gameRepository.save(game);
            });
        }
    }

    private Order findByPaymentIntentId(String paymentIntentId) {
        return orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Aucune commande associee au paiement " + paymentIntentId));
    }
}
