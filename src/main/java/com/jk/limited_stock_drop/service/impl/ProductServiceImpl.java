package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.request.ProductFilterRequest;
import com.jk.limited_stock_drop.dto.product.request.UpdateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.dto.product.response.ProductSummaryResponse;
import com.jk.limited_stock_drop.dto.product.response.StockResponse;
import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.enums.OrderStatus;
import com.jk.limited_stock_drop.exception.BusinessException;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.mapper.ProductMapper;
import com.jk.limited_stock_drop.queryService.OrderQueryService;
import com.jk.limited_stock_drop.repository.ProductRepository;
import com.jk.limited_stock_drop.service.ProductService;
import com.jk.limited_stock_drop.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final OrderQueryService orderQueryService;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ProductSummaryResponse> getAllProducts(ProductFilterRequest filterRequest) {

        Specification<Product> spec = Specification
                .where(ProductSpecification.hasSearch(filterRequest.getSearch()))
                .and(ProductSpecification.hasCategory(filterRequest.getCategory()))
                .and(ProductSpecification.isInStock(filterRequest.getInStock()))
                .and(ProductSpecification.hasPriceBetween(filterRequest.getMinPrice(), filterRequest.getMaxPrice()));


        Sort sort = filterRequest.getSortDirection().equalsIgnoreCase("desc")
                ? Sort.by(filterRequest.getSortBy()).descending()
                : Sort.by(filterRequest.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Product> productPage = productRepository.findAll(spec, pageable);

        log.info("[PRODUCT-SERVICE] Products fetched | found={}, totalPages={}, page={}, size={}",
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                filterRequest.getPage(),
                filterRequest.getSize());

        Page<ProductSummaryResponse> responsePage = productPage.map(ProductMapper::toSummaryResponse);
        return new PaginatedResponse<>(responsePage);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getProductById(Long productId) {
        Product product = findByIdOrThrow(productId);
        log.info("[PRODUCT-SERVICE] Product fetched | id={}", productId);
        return ProductMapper.toResponse(product);
    }

    @Override
    public StockResponse getStockForProductId(Long productId) {
        Product product = findByIdOrThrow(productId);
        log.info("[PRODUCT-SERVICE] Product stock fetched | id={}", productId);
        return ProductMapper.toStockResponse(product);
    }


    // ==========================================
    //          ADMIN-ONLY METHODS
    // ==========================================
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse createProduct(CreateProductRequest request) {
        Product product = ProductMapper.toEntity(request);

        product = productRepository.save(product);
        log.info("[PRODUCT-SERVICE] Product created | id={}", product.getId());

        return ProductMapper.toResponse(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProductResponse updateProduct(Long productId, UpdateProductRequest updateRequest) {
        Product product = findByIdOrThrow(productId);
        populateNonNullFields(product, updateRequest);
        productRepository.save(product);

        log.info("[PRODUCT-SERVICE] Product updated | id={}", productId);
        return ProductMapper.toResponse(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProduct(Long productId) {
        Product product = findByIdOrThrow(productId);
        // Guard 1 — confirmed orders exist
        if (orderQueryService.hasConfirmedOrders(productId)) {
            log.warn("[PRODUCT-SERVICE] Delete rejected — confirmed orders exist | productId={}", productId);
            throw new BusinessException("Cannot delete product with confirmed orders");
        }

        // Guard 2 — active reservations exist
        if (product.getReservedStock() > 0) {
            log.warn("[PRODUCT-SERVICE] Delete rejected — active reservations exist | productId={} reservedStock={}",
                    productId, product.getReservedStock());
            throw new BusinessException("Cannot delete product with active reservations");
        }

        // Soft delete — preserves order history integrity
        product.setActive(false);
        productRepository.save(product);

        log.info("[PRODUCT-SERVICE] Product soft-deleted | id={}", productId);
    }


    // ==========================================
    //          HELPER METHODS
    // ==========================================

    private Product findByIdOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("[PRODUCT-SERVICE] Product not found with ID: {}", productId);
                    return new ResourceNotFoundException("Product not found");
                });
    }

    private void populateNonNullFields(Product product, UpdateProductRequest updateRequest) {
        if(updateRequest.getName() != null) product.setName(updateRequest.getName());
        if(updateRequest.getPrice() != null) product.setPrice(updateRequest.getPrice());
        if(updateRequest.getDescription() != null) product.setDescription(updateRequest.getDescription());
        if(updateRequest.getImageKey() != null) product.setImageKey(updateRequest.getImageKey());
        if(updateRequest.getActive() != null) product.setActive(updateRequest.getActive());
        if(updateRequest.getCategory() != null) product.setCategory(updateRequest.getCategory());

        if (updateRequest.getTotalStock() != null) {
            int newTotal = updateRequest.getTotalStock();
            int lockedStock = product.getTotalStock() - product.getAvailableStock();

            if (newTotal < lockedStock) {
                throw new BusinessException(
                        "Cannot reduce total stock below currently reserved quantity of " + lockedStock
                );
            }
            product.setTotalStock(newTotal);
        }
    }
}
