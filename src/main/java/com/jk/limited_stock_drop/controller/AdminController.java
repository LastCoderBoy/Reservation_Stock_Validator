package com.jk.limited_stock_drop.controller;

import com.jk.limited_stock_drop.dto.ApiResponse;
import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.request.UpdateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.jk.limited_stock_drop.utils.AppConstants.ADMIN_PATH;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ADMIN_PATH)
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ProductService productService;

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        log.info("[PRODUCT-CONTROLLER] Creating product with name: {}", request.getName());

        ProductResponse productResponse = productService.createProduct(request);
        return ResponseEntity.ok(
                ApiResponse.success("Product created successfully", productResponse)
        );
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long productId,
                                                                      @Valid @RequestBody UpdateProductRequest updateRequest){
        log.info("[PRODUCT-CONTROLLER] Updating product with ID: {}", productId);

        ProductResponse productResponse = productService.updateProduct(productId, updateRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", productResponse)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId){
        log.info("[PRODUCT-CONTROLLER] Deleting product with ID: {}", productId);

        productService.deleteProduct(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully")
        );
    }
}
