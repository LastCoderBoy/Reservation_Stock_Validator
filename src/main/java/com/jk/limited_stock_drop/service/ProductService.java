package com.jk.limited_stock_drop.service;

import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.request.ProductFilterRequest;
import com.jk.limited_stock_drop.dto.product.request.UpdateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.dto.product.response.ProductSummaryResponse;
import com.jk.limited_stock_drop.dto.product.response.StockResponse;

public interface ProductService {
    PaginatedResponse<ProductSummaryResponse> getAllProducts(ProductFilterRequest filterRequest);

    ProductResponse getProductById(Long productId);

    StockResponse getStockForProductId(Long productId);

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(Long productId, UpdateProductRequest updateRequest);

    void deleteProduct(Long productId);
}
