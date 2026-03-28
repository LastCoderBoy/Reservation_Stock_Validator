package com.jk.limited_stock_drop.repository;

import com.jk.limited_stock_drop.entity.Order;
import com.jk.limited_stock_drop.enums.OrderStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN FETCH o.product WHERE o.product.id = :productId ")
    List<Order> findAllByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.product.id = :productId AND o.status = :status")
    boolean existsByProductIdAndStatus(
            @Param("productId") Long productId,
            @Param("status") OrderStatus status
    );
}
