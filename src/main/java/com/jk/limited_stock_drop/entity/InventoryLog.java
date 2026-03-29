package com.jk.limited_stock_drop.entity;

import com.jk.limited_stock_drop.enums.InventoryAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_logs",
    indexes = {
        @Index(name = "idx_inventory_product", columnList = "product_id"),
        @Index(name = "idx_inventory_action", columnList = "action"),
        @Index(name = "idx_inventory_created", columnList = "created_at")
    })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryAction action;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Factory method for reservation creation
    public static InventoryLog forReservation(Product product, Reservation reservation, int stockBefore) {
        return InventoryLog.builder()
            .product(product)
            .reservation(reservation)
            .action(InventoryAction.STOCK_RESERVED)
            .quantityChange(reservation.getQuantity())
            .stockBefore(stockBefore)
            .stockAfter(stockBefore - reservation.getQuantity())
            .description("Stock reserved for reservation")
            .build();
    }

    // Factory method for reservation expiration
    public static InventoryLog forExpiration(Product product, Reservation reservation, int stockBefore) {
        return InventoryLog.builder()
            .product(product)
            .reservation(reservation)
            .action(InventoryAction.STOCK_RELEASED)
            .quantityChange(reservation.getQuantity())
            .stockBefore(stockBefore)
            .stockAfter(stockBefore + reservation.getQuantity())
            .description("Stock released from expired reservation")
            .build();
    }

    // Factory method for order confirmation
    public static InventoryLog forOrderConfirm(Product product, Order order, int stockBefore) {
        return InventoryLog.builder()
            .product(product)
            .order(order)
            .action(InventoryAction.ORDER_CONFIRMED)
            .quantityChange(order.getQuantity())
            .stockBefore(stockBefore)
            .stockAfter(stockBefore - order.getQuantity())
            .description("Stock permanently deducted for completed order")
            .build();
    }
}