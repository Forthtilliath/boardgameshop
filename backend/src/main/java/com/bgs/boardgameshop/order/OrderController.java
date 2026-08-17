package com.bgs.boardgameshop.order;

import com.bgs.boardgameshop.order.dto.CreateOrderRequest;
import com.bgs.boardgameshop.order.dto.OrderResponse;
import com.bgs.boardgameshop.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal SecurityUser principal
    ) {
        return orderService.createOrder(request, principal.getUser());
    }

    /** Historique des commandes de l'utilisateur connecté (page "Mon compte"). */
    @GetMapping
    public List<OrderResponse> listMyOrders(@AuthenticationPrincipal SecurityUser principal) {
        return orderService.listMyOrders(principal.getUser());
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal) {
        return orderService.getOrder(id, principal.getUser());
    }

    @GetMapping("/{id}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long id, @AuthenticationPrincipal SecurityUser principal) {
        byte[] pdf = orderService.downloadInvoice(id, principal.getUser());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=\"facture-bgs-" + id + ".pdf\"")
                .body(pdf);
    }
}
