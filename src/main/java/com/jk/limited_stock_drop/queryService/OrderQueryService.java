package com.jk.limited_stock_drop.queryService;

import com.jk.limited_stock_drop.entity.Order;
import com.jk.limited_stock_drop.enums.OrderStatus;
import com.jk.limited_stock_drop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Order> getOrdersByProductId(Long productId) {
        return orderRepository.findAllByProductId(productId);
    }

    @Transactional(readOnly = true)
    public boolean hasConfirmedOrders(Long productId) {
        return orderRepository.existsByProductIdAndStatus(productId, OrderStatus.CONFIRMED);
    }
}
