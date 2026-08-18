package com.bgs.boardgameshop.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatusIn(List<OrderStatus> statuses);

    /** Pour le graphe "par jour" du dashboard admin (voir AdminStatsService). */
    List<Order> findByStatusInAndCreatedAtAfter(List<OrderStatus> statuses, Instant since);

    /** "Meilleure commande" du dashboard admin : la plus grosse commande réellement payée. */
    Optional<Order> findFirstByStatusInOrderByTotalAmountDesc(List<OrderStatus> statuses);
}
