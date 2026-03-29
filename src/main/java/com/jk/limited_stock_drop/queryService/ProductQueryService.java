package com.jk.limited_stock_drop.queryService;

import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductQueryService {

    private final ProductRepository productRepository;

    /**
     * Find product with pessimistic lock for reservation scenarios
     * Used during high-contention operations like stock reservation
     */
    @Transactional
    public Product findByIdWithLockOrThrow(Long productId) {
        return productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT-QUERY-SERVICE] Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found");
                });
    }
}
