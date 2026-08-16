package com.bgs.boardgameshop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrder_StatusIn(List<OrderStatus> statuses);

    @Query("""
            SELECT COUNT(ol) > 0 FROM OrderLine ol
            WHERE ol.order.user.id = :userId
            AND ol.gameId = :gameId
            AND ol.order.status IN :statuses
            """)
    boolean existsPurchase(@Param("userId") Long userId, @Param("gameId") Long gameId, @Param("statuses") List<OrderStatus> statuses);
}
