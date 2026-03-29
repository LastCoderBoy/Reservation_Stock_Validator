package com.jk.limited_stock_drop.mapper;

import com.jk.limited_stock_drop.dto.reservation.response.CheckoutResponse;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;
import com.jk.limited_stock_drop.entity.Order;
import com.jk.limited_stock_drop.entity.Reservation;

public class ReservationMapper {

    private ReservationMapper() {
        throw new UnsupportedOperationException("ReservationMapper is a utility class and cannot be instantiated");
    }

    public static ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .productId(reservation.getProduct().getId())
                .productName(reservation.getProduct().getName())
                .productImageKey(reservation.getProduct().getImageKey())
                .quantity(reservation.getQuantity())
                .status(reservation.getStatus())
                .expiresAt(reservation.getExpiresAt())
                .remainingSeconds(reservation.getRemainingSeconds())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    public static CheckoutResponse toCheckoutResponse(Order order) {
        return CheckoutResponse.builder()
                .orderId(order.getId())
                .reservationId(order.getReservation().getId())
                .productId(order.getProduct().getId())
                .productName(order.getProduct().getName())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
