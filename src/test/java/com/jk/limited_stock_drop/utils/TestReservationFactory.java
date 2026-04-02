package com.jk.limited_stock_drop.utils;

import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.entity.Reservation;
import com.jk.limited_stock_drop.entity.User;
import com.jk.limited_stock_drop.enums.ReservationStatus;

import java.time.LocalDateTime;

/**
 * Test fixture factory for Reservation entities.
 * Provides pre-configured Reservation objects for unit and integration tests.
 */
public final class TestReservationFactory {

    private TestReservationFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a standard pending reservation with 5 minutes until expiry.
     */
    public static Reservation createPendingReservation(User user, Product product) {
        return Reservation.builder()
                .id(1000L)
                .user(user)
                .product(product)
                .quantity(2)
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a pending reservation with custom quantity.
     */
    public static Reservation createPendingReservationWithQuantity(User user, Product product, int quantity) {
        return Reservation.builder()
                .id(1001L)
                .user(user)
                .product(product)
                .quantity(quantity)
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a pending reservation with custom ID and quantity.
     */
    public static Reservation createPendingReservation(Long id, User user, Product product, int quantity) {
        return Reservation.builder()
                .id(id)
                .user(user)
                .product(product)
                .quantity(quantity)
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates an expired reservation (expiry time in the past).
     */
    public static Reservation createExpiredReservation(User user, Product product) {
        return Reservation.builder()
                .id(1002L)
                .user(user)
                .product(product)
                .quantity(1)
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now().minusMinutes(6))
                .updatedAt(LocalDateTime.now().minusMinutes(6))
                .build();
    }

    /**
     * Creates a reservation marked as EXPIRED status.
     */
    public static Reservation createExpiredStatusReservation(User user, Product product) {
        return Reservation.builder()
                .id(1003L)
                .user(user)
                .product(product)
                .quantity(1)
                .status(ReservationStatus.EXPIRED)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now().minusMinutes(6))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a confirmed reservation.
     */
    public static Reservation createConfirmedReservation(User user, Product product) {
        return Reservation.builder()
                .id(1004L)
                .user(user)
                .product(product)
                .quantity(2)
                .status(ReservationStatus.CONFIRMED)
                .expiresAt(LocalDateTime.now().minusMinutes(2))
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a cancelled reservation.
     */
    public static Reservation createCancelledReservation(User user, Product product) {
        return Reservation.builder()
                .id(1005L)
                .user(user)
                .product(product)
                .quantity(1)
                .status(ReservationStatus.CANCELLED)
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a reservation about to expire (30 seconds left).
     */
    public static Reservation createAboutToExpireReservation(User user, Product product) {
        return Reservation.builder()
                .id(1006L)
                .user(user)
                .product(product)
                .quantity(1)
                .status(ReservationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusSeconds(30))
                .createdAt(LocalDateTime.now().minusMinutes(4).minusSeconds(30))
                .updatedAt(LocalDateTime.now().minusMinutes(4).minusSeconds(30))
                .build();
    }

    /**
     * Creates a reservation with custom expiry time.
     */
    public static Reservation createReservationWithExpiry(User user, Product product, LocalDateTime expiresAt) {
        return Reservation.builder()
                .id(1007L)
                .user(user)
                .product(product)
                .quantity(1)
                .status(ReservationStatus.PENDING)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
