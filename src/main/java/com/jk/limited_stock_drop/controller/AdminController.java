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
        log.info("ADMIN - Creating product - Name: '{}', Stock: {}, Price: {}", 
                request.getName(), request.getTotalStock(), request.getPrice());

        ProductResponse productResponse = productService.createProduct(request);
        
        log.info("ADMIN - Product created successfully - ID: {}, Name: '{}'", 
                productResponse.getId(), productResponse.getName());
        
        return ResponseEntity.ok(
                ApiResponse.success("Product created successfully", productResponse)
        );
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long productId,
                                                                      @Valid @RequestBody UpdateProductRequest updateRequest){
        log.info("ADMIN - Updating product - ID: {}", productId);

        ProductResponse productResponse = productService.updateProduct(productId, updateRequest);
        
        log.info("ADMIN - Product updated successfully - ID: {}, Name: '{}'", 
                productResponse.getId(), productResponse.getName());
        
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", productResponse)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long productId){
        log.warn("ADMIN - Deleting product - ID: {}", productId);

        productService.deleteProduct(productId);
        
        log.info("ADMIN - Product deleted successfully - ID: {}", productId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully")
        );
    }
}
