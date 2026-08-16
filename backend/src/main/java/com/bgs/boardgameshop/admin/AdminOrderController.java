package com.bgs.boardgameshop.admin;

import com.bgs.boardgameshop.admin.dto.AdminOrderResponse;
import com.bgs.boardgameshop.admin.dto.UpdateOrderStatusRequest;
import com.bgs.boardgameshop.order.OrderStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public List<AdminOrderResponse> getOrders(@RequestParam(required = false) OrderStatus status) {
        return adminOrderService.getOrders(status);
    }

    @PutMapping("/{id}/status")
    public AdminOrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return adminOrderService.updateStatus(id, request.status());
    }
}
