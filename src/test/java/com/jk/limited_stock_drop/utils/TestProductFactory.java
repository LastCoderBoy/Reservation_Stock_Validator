package com.jk.limited_stock_drop.utils;

import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.enums.Category;

import java.math.BigDecimal;

/**
 * Test fixture factory for Product entities.
 * Provides pre-configured Product objects for unit and integration tests.
 */
public final class TestProductFactory {

    private TestProductFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a standard product with 10 stock available.
     */
    public static Product createDefaultProduct() {
        return Product.builder()
                .id(100L)
                .name("Limited Edition Sneakers")
                .description("Exclusive release footwear")
                .price(BigDecimal.valueOf(199.99))
                .totalStock(10)
                .reservedStock(0)
                .category(Category.MEN)
                .active(true)
                .imageKey("sneakers-001.jpg")
                .build();
    }

    /**
     * Creates a product with custom ID and stock.
     */
    public static Product createProductWithIdAndStock(Long id, int totalStock, int reservedStock) {
        return Product.builder()
                .id(id)
                .name("Product " + id)
                .description("Description for product " + id)
                .price(BigDecimal.valueOf(99.99))
                .totalStock(totalStock)
                .reservedStock(reservedStock)
                .category(Category.KIDS)
                .active(true)
                .imageKey("product-" + id + ".jpg")
                .build();
    }

    /**
     * Creates a product with custom price.
     */
    public static Product createProductWithPrice(BigDecimal price) {
        return Product.builder()
                .id(101L)
                .name("Premium Item")
                .description("High-value product")
                .price(price)
                .totalStock(5)
                .reservedStock(0)
                .category(Category.WOMEN)
                .active(true)
                .imageKey("premium-item.jpg")
                .build();
    }

    /**
     * Creates a product with low stock (only 2 available).
     */
    public static Product createLowStockProduct() {
        return Product.builder()
                .id(102L)
                .name("Rare Collectible")
                .description("Very limited stock")
                .price(BigDecimal.valueOf(499.99))
                .totalStock(2)
                .reservedStock(0)
                .category(Category.WOMEN)
                .active(true)
                .imageKey("collectible.jpg")
                .build();
    }

    /**
     * Creates a product that is out of stock (all reserved).
     */
    public static Product createOutOfStockProduct() {
        return Product.builder()
                .id(103L)
                .name("Sold Out Item")
                .description("No stock available")
                .price(BigDecimal.valueOf(149.99))
                .totalStock(5)
                .reservedStock(5)
                .category(Category.KIDS)
                .active(true)
                .imageKey("sold-out.jpg")
                .build();
    }

    /**
     * Creates an inactive/disabled product.
     */
    public static Product createInactiveProduct() {
        return Product.builder()
                .id(104L)
                .name("Discontinued Item")
                .description("No longer available")
                .price(BigDecimal.valueOf(79.99))
                .totalStock(10)
                .reservedStock(0)
                .category(Category.WOMEN)
                .active(false)
                .imageKey("discontinued.jpg")
                .build();
    }

    /**
     * Creates a product in APPAREL category.
     */
    public static Product createApparelProduct() {
        return Product.builder()
                .id(105L)
                .name("Limited T-Shirt")
                .description("Exclusive apparel")
                .price(BigDecimal.valueOf(49.99))
                .totalStock(20)
                .reservedStock(0)
                .category(Category.MEN)
                .active(true)
                .imageKey("tshirt.jpg")
                .build();
    }

    public static Product createHoodiesProduct() {
        return Product.builder()
                .id(106L)
                .name("Limited Edition Hoodies")
                .description("The best hoodies ever!")
                .price(BigDecimal.valueOf(299.99))
                .totalStock(15)
                .reservedStock(0)
                .category(Category.MEN)
                .active(true)
                .imageKey("hodies_premium.jpg")
                .build();
    }
}
