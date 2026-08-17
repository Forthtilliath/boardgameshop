package com.bgs.boardgameshop.order;

import com.bgs.boardgameshop.game.Game;
import com.bgs.boardgameshop.game.GameNotFoundException;
import com.bgs.boardgameshop.game.GameRepository;
import com.bgs.boardgameshop.order.dto.CreateOrderRequest;
import com.bgs.boardgameshop.order.dto.OrderItemRequest;
import com.bgs.boardgameshop.order.dto.OrderResponse;
import com.bgs.boardgameshop.promocode.PromoCode;
import com.bgs.boardgameshop.promocode.PromoCodeService;
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

    private static final List<OrderStatus> INVOICEABLE_STATUSES =
            List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);

    private final OrderRepository orderRepository;
    private final GameRepository gameRepository;
    private final InvoiceService invoiceService;
    private final PromoCodeService promoCodeService;

    public OrderService(
            OrderRepository orderRepository,
            GameRepository gameRepository,
            InvoiceService invoiceService,
            PromoCodeService promoCodeService
    ) {
        this.orderRepository = orderRepository;
        this.gameRepository = gameRepository;
        this.invoiceService = invoiceService;
        this.promoCodeService = promoCodeService;
    }

    /**
     * Crée une commande à partir du panier envoyé par le client et décrémente
     * (réserve) le stock des jeux commandés. La commande reste EN_ATTENTE_PAIEMENT
     * jusqu'à confirmation du paiement Stripe (voir markAsPaid/markAsFailed).
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
            // Verrou pessimiste : deux commandes concurrentes sur le meme jeu ne peuvent
            // pas lire/decrementer le stock en meme temps (evite la survente du dernier
            // exemplaire). Voir GameRepository#findByIdForUpdate.
            Game game = gameRepository.findByIdForUpdate(item.gameId())
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

        // Le code promo est revalidé ici (jamais fait confiance au pourcentage vu côté
        // client) : si invalide, l'ensemble de la commande échoue (rollback transactionnel,
        // le stock déjà décrémenté ci-dessus est annulé avec).
        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            PromoCode promoCode = promoCodeService.validateAndGet(request.promoCode());
            BigDecimal discount = total
                    .multiply(BigDecimal.valueOf(promoCode.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
            total = total.subtract(discount);
            order.setPromoCode(promoCode.getCode());
            order.setDiscountAmount(discount);
            promoCodeService.incrementUsage(promoCode);
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id, User currentUser) {
        return OrderResponse.fromEntity(getOwnedOrder(id, currentUser));
    }

    /** Historique de commandes de l'utilisateur courant, les plus récentes en premier. */
    @Transactional(readOnly = true)
    public List<OrderResponse> listMyOrders(User currentUser) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId()).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    /** Facture PDF, uniquement pour une commande dont le paiement a été confirmé. */
    @Transactional(readOnly = true)
    public byte[] downloadInvoice(Long id, User currentUser) {
        Order order = getOwnedOrder(id, currentUser);
        if (!INVOICEABLE_STATUSES.contains(order.getStatus())) {
            throw new InvoiceNotAvailableException(id);
        }
        return invoiceService.generateInvoice(order);
    }

    /**
     * Récupère la commande si elle appartient à l'utilisateur (ou s'il est admin),
     * pour usage interne (ex : PaymentService avant de créer un Payment Intent).
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
     * Le paiement a échoué : la commande est marquée ECHOUEE et le stock
     * réservé à la création de la commande est restitué.
     */
    @Transactional
    public void markAsFailed(String paymentIntentId) {
        Order order = findByPaymentIntentId(paymentIntentId);
        order.setStatus(OrderStatus.ECHOUEE);
        restituteStock(order);
        orderRepository.save(order);
    }

    /**
     * Liste des commandes pour le dashboard admin (toutes, ou filtrées par
     * statut), les plus récentes en premier.
     */
    @Transactional(readOnly = true)
    public List<Order> adminListOrders(OrderStatus statusFilter) {
        return statusFilter == null
                ? orderRepository.findAllByOrderByCreatedAtDesc()
                : orderRepository.findByStatusOrderByCreatedAtDesc(statusFilter);
    }

    /**
     * Fait progresser une commande dans son cycle de vie (PAYEE -> EXPEDIEE ->
     * LIVREE, ou annulation). Restitue le stock si la commande est annulée
     * après avoir été payée.
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
            gameRepository.findByIdForUpdate(line.getGameId()).ifPresent(game -> {
                game.setStock(game.getStock() + line.getQuantity());
                gameRepository.save(game);
            });
        }
    }

    private Order findByPaymentIntentId(String paymentIntentId) {
        return orderRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Aucune commande associée au paiement " + paymentIntentId));
    }
}
