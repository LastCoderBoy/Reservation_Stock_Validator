package com.jk.limited_stock_drop.dto.product.request;

import com.jk.limited_stock_drop.enums.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

import static com.jk.limited_stock_drop.utils.AppConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFilterRequest {

    @Builder.Default
    @PositiveOrZero
    private int page = DEFAULT_PAGE_NUMBER;

    @Builder.Default
    @Positive
    @Max(100)
    private int size = DEFAULT_PAGE_SIZE;

    @Builder.Default
    @Pattern(regexp = "name|price|stock", message = "Invalid sort field. Must be either 'name', 'price', or 'stock'")
    private String sortBy = DEFAULT_SORT_BY;

    @Builder.Default
    @Pattern(regexp = "asc|desc", message = "Invalid sort direction. Must be either 'asc' or 'desc'")
    private String sortDirection = DEFAULT_SORT_DIRECTION;

    @Size(max = 100, message = "Search term must not exceed 100 characters")
    private String search;

    private Category category;
    private Boolean inStock;

    @DecimalMin("0.0")
    private BigDecimal minPrice;

    @DecimalMin("0.0")
    private BigDecimal maxPrice;
}
