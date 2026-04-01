package com.jk.limited_stock_drop.controller;

import com.jk.limited_stock_drop.dto.ApiResponse;
import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.request.ProductFilterRequest;
import com.jk.limited_stock_drop.dto.product.request.UpdateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.dto.product.response.ProductSummaryResponse;
import com.jk.limited_stock_drop.dto.product.response.StockResponse;
import com.jk.limited_stock_drop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.jk.limited_stock_drop.utils.AppConstants.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PRODUCTS_PATH)
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductSummaryResponse>>> getAllProducts(
            @ModelAttribute @Valid ProductFilterRequest filterRequest
    ) {
        log.info("[PRODUCT-CONTROLLER] Fetching products with filters: page={}, size={}, sortDirection={}, sortBy={}," +
                "search={}, category={}, inStock={}, minPrice={}, maxPrice={}", filterRequest.getPage(), filterRequest.getSize(),
                filterRequest.getSortDirection(), filterRequest.getSortBy(), filterRequest.getSearch(), filterRequest.getCategory(),
                filterRequest.getInStock(), filterRequest.getMinPrice(), filterRequest.getMaxPrice());

        PaginatedResponse<ProductSummaryResponse> productSummaryResponse =
               productService.getAllProducts(filterRequest);

       return ResponseEntity.ok(
               ApiResponse.success("Products fetched successfully", productSummaryResponse)
       );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long productId) {
        log.info("[PRODUCT-CONTROLLER] Fetching product with ID: {}", productId);

        ProductResponse productResponse = productService.getProductById(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", productResponse)
        );
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<StockResponse>> getStockForProductId(@PathVariable Long productId) {
        log.info("[PRODUCT-CONTROLLER] Fetching product stock for ID: {}", productId);

        StockResponse stockResponse = productService.getStockForProductId(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Stock information retrieved", stockResponse)
        );
    }
}
