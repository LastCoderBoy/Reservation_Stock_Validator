package com.jk.limited_stock_drop.entity;

import com.jk.limited_stock_drop.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations",
    indexes = {
        @Index(name = "idx_reservation_user", columnList = "user_id"),
        @Index(name = "idx_reservation_product", columnList = "product_id"),
        @Index(name = "idx_reservation_status", columnList = "status"),
        @Index(name = "idx_reservation_expires", columnList = "expires_at")
    })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY - we only load the user and product when we need them, not every time we load a reservation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ====================================
    //          HELPER METHODS
    // ====================================

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == ReservationStatus.PENDING && !isExpired();
    }

    public long getRemainingSeconds() {
        if (isExpired() || status != ReservationStatus.PENDING) {
            return 0;
        }
        return Math.max(0, Duration.between(LocalDateTime.now(), expiresAt).getSeconds());
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public void expire() {
        this.status = ReservationStatus.EXPIRED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}