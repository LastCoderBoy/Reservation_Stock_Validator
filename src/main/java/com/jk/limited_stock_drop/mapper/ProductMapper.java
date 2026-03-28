package com.jk.limited_stock_drop.mapper;

import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.dto.product.response.ProductSummaryResponse;
import com.jk.limited_stock_drop.dto.product.response.StockResponse;
import com.jk.limited_stock_drop.entity.Product;

public class ProductMapper {

    private ProductMapper() {
        throw new UnsupportedOperationException("ProductMapper is a utility class and cannot be instantiated");
    }

    public static Product toEntity(CreateProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageKey(request.getImageKey())
                .totalStock(request.getTotalStock())
                .reservedStock(0)
                .category(request.getCategory())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageKey(product.getImageKey())
                .totalStock(product.getTotalStock())
                .reservedStock(product.getReservedStock())
                .availableStock(product.getAvailableStock())
                .category(product.getCategory())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static ProductSummaryResponse toSummaryResponse(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imageKey(product.getImageKey())
                .availableStock(product.getAvailableStock())
                .category(product.getCategory())
                .active(product.getActive())
                .build();
    }

    public static StockResponse toStockResponse(Product product) {
        return StockResponse.of(
                product.getId(),
                product.getName(),
                product.getTotalStock(),
                product.getReservedStock(),
                product.getAvailableStock(),
                product.getActive()
        );
    }
}
