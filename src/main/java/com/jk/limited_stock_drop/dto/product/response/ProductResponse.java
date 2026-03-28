package com.jk.limited_stock_drop.dto.product.response;

import com.jk.limited_stock_drop.enums.Category;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageKey;
    private Integer totalStock;
    private Integer reservedStock;
    private Integer availableStock;
    private Category category;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
