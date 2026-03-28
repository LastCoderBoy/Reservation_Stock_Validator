package com.jk.limited_stock_drop.entity;

import com.jk.limited_stock_drop.enums.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products",
    indexes = {
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_active", columnList = "active")
    })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "image_key", length = 255)
    private String imageKey;

    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private Integer reservedStock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Derived field - not persisted
    @Transient
    public Integer getAvailableStock() {
        return totalStock - reservedStock;
    }

    // ===================================
    // HELPER METHODS FOR RESERVATION
    // ===================================
    public boolean canReserve(int quantity) {
        return getAvailableStock() >= quantity;
    }

    public void reserveStock(int quantity) {
        if (!canReserve(quantity)) {
            throw new IllegalStateException("Insufficient stock available");
        }
        this.reservedStock += quantity;
    }

    public void releaseStock(int quantity) {
        if (this.reservedStock < quantity) {
            throw new IllegalStateException("Cannot release more stock than reserved");
        }
        this.reservedStock -= quantity;
    }
}
