package com.jk.limited_stock_drop.enums;

public enum InventoryAction {
    STOCK_RESERVED,      // Stock reserved for a reservation
    STOCK_RELEASED,      // Stock released from expired/cancelled reservation
    ORDER_CONFIRMED,     // Order confirmed, stock permanently deducted
    STOCK_ADJUSTED       // Manual stock adjustment by admin
}