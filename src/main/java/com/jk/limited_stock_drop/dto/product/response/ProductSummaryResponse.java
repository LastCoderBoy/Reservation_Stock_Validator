package com.jk.limited_stock_drop.dto.product.response;

import com.jk.limited_stock_drop.enums.Category;
import lombok.*;

import java.math.BigDecimal;

/**
 * Lightweight product response for list views
 * Excludes description and timestamps for better performance
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSummaryResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private String imageKey;
    private Integer availableStock;
    private Category category;
    private Boolean active;
}
