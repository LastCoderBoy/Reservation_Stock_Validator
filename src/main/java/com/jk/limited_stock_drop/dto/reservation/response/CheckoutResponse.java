package com.jk.limited_stock_drop.dto.reservation.response;

import com.jk.limited_stock_drop.enums.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {

    private Long orderId;
    private Long reservationId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
}
