package com.jk.limited_stock_drop.enums;

public enum ReservationStatus {
    PENDING,      // Reservation created, waiting for checkout
    CONFIRMED,    // Reservation converted to order
    EXPIRED,      // Reservation expired (5 min timeout)
    CANCELLED     // Reservation cancelled by user
}