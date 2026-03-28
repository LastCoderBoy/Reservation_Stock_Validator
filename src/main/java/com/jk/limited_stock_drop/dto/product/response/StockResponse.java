package com.jk.limited_stock_drop.dto.product.response;

import lombok.*;

/**
 * Real-time stock information for a product
 * Used by frontend to update UI every 5 seconds
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockResponse {

    private Long productId;
    private String productName;
    private Integer totalStock;
    private Integer reservedStock;
    private Integer availableStock;
    private Boolean inStock;
    private Boolean active;

    public static StockResponse of(Long productId, String productName, Integer totalStock, 
                                     Integer reservedStock, Integer availableStock, Boolean active) {
        return StockResponse.builder()
                .productId(productId)
                .productName(productName)
                .totalStock(totalStock)
                .reservedStock(reservedStock)
                .availableStock(availableStock)
                .inStock(availableStock > 0)
                .active(active)
                .build();
    }
}
