package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminOrderResponse;
import com.bgs.boardgameshop.order.OrderService;
import com.bgs.boardgameshop.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Enrobe OrderService pour la vue admin : le mapping vers AdminOrderResponse
 * (qui touche des collections/relations lazy : lignes, utilisateur) doit se
 * faire dans la meme transaction que le chargement, d'ou ce service dedie.
 */
@Service
public class AdminOrderService {

    private final OrderService orderService;

    public AdminOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public List<AdminOrderResponse> getOrders(OrderStatus status) {
        return orderService.adminListOrders(status).stream().map(AdminOrderResponse::fromEntity).toList();
    }

    @Transactional
    public AdminOrderResponse updateStatus(Long id, OrderStatus newStatus) {
        return AdminOrderResponse.fromEntity(orderService.adminUpdateStatus(id, newStatus));
    }
}
