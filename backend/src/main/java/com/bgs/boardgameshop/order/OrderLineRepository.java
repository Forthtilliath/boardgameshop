package com.bgs.boardgameshop.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrder_StatusIn(List<OrderStatus> statuses);
}
