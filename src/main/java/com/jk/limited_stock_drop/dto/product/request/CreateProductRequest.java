package com.jk.limited_stock_drop.dto.product.request;

import com.jk.limited_stock_drop.enums.Category;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
    private String name;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Price must have at most 17 digits and 2 decimal places")
    private BigDecimal price;

    @Size(max = 255, message = "Image key cannot exceed 255 characters")
    private String imageKey;

    @NotNull(message = "Total stock is required")
    @Min(value = 0, message = "Total stock cannot be negative")
    @Max(value = 1_000_000, message = "Total stock cannot exceed 1,000,000")
    private Integer totalStock;

    @NotNull(message = "Category is required")
    private Category category;

    @Builder.Default
    private Boolean active = true;
}
