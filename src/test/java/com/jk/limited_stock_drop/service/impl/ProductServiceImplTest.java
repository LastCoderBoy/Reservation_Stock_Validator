package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.dto.product.request.CreateProductRequest;
import com.jk.limited_stock_drop.dto.product.request.UpdateProductRequest;
import com.jk.limited_stock_drop.dto.product.response.ProductResponse;
import com.jk.limited_stock_drop.dto.product.response.StockResponse;
import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.enums.Category;
import com.jk.limited_stock_drop.exception.BusinessException;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.queryService.OrderQueryService;
import com.jk.limited_stock_drop.repository.ProductRepository;
import com.jk.limited_stock_drop.utils.TestProductFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderQueryService orderQueryService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = TestProductFactory.createDefaultProduct();
    }

    // =========================================================================
    //                         GET PRODUCT BY ID TESTS
    // =========================================================================

    @Nested
    @DisplayName("getProductById()")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return product when found")
        void shouldReturnProductWhenFound() {
            // Given
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

            // When
            ProductResponse response = productService.getProductById(100L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(100L);
            assertThat(response.getName()).isEqualTo("Limited Edition Sneakers");
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(199.99));
            assertThat(response.getTotalStock()).isEqualTo(10);
            assertThat(response.getAvailableStock()).isEqualTo(10);
            assertThat(response.getCategory()).isEqualTo(Category.MEN);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFound() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    // =========================================================================
    //                         GET STOCK TESTS
    // =========================================================================

    @Nested
    @DisplayName("getStockForProductId()")
    class GetStockTests {

        @Test
        @DisplayName("should return stock information")
        void shouldReturnStockInfo() {
            // Given
            testProduct.setReservedStock(3);
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

            // When
            StockResponse response = productService.getStockForProductId(100L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotalStock()).isEqualTo(10);
            assertThat(response.getReservedStock()).isEqualTo(3);
            assertThat(response.getAvailableStock()).isEqualTo(7);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFoundForStock() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.getStockForProductId(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }
    }

    // =========================================================================
    //                         CREATE PRODUCT TESTS
    // =========================================================================

    @Nested
    @DisplayName("createProduct()")
    class CreateProductTests {

        @Test
        @DisplayName("should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Given
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("New Sneakers")
                    .description("Brand new release")
                    .price(BigDecimal.valueOf(149.99))
                    .totalStock(50)
                    .category(Category.KIDS)
                    .imageKey("new-sneakers.jpg")
                    .active(true)
                    .build();

            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product saved = invocation.getArgument(0);
                saved.setId(200L);
                return saved;
            });

            // When
            ProductResponse response = productService.createProduct(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(200L);
            assertThat(response.getName()).isEqualTo("New Sneakers");
            assertThat(response.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.99));
            assertThat(response.getTotalStock()).isEqualTo(50);

            verify(productRepository).save(productCaptor.capture());
            Product savedProduct = productCaptor.getValue();
            assertThat(savedProduct.getReservedStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("should create product with default active status")
        void shouldCreateProductWithDefaultActive() {
            // Given
            CreateProductRequest request = CreateProductRequest.builder()
                    .name("Test Product")
                    .price(BigDecimal.valueOf(99.99))
                    .totalStock(10)
                    .category(Category.WOMEN)
                    .build();

            when(productRepository.save(any(Product.class))).thenAnswer(i -> {
                Product p = i.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When
            ProductResponse response = productService.createProduct(request);

            // Then
            assertThat(response.getActive()).isTrue();
        }
    }

    // =========================================================================
    //                         UPDATE PRODUCT TESTS
    // =========================================================================

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProductTests {

        @Test
        @DisplayName("should update product name only")
        void shouldUpdateNameOnly() {
            // Given
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Updated Name")
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            ProductResponse response = productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getName()).isEqualTo("Updated Name");
            assertThat(testProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(199.99)); // unchanged
        }

        @Test
        @DisplayName("should update multiple fields")
        void shouldUpdateMultipleFields() {
            // Given
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("Updated Sneakers")
                    .price(BigDecimal.valueOf(249.99))
                    .description("New description")
                    .category(Category.KIDS)
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getName()).isEqualTo("Updated Sneakers");
            assertThat(testProduct.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(249.99));
            assertThat(testProduct.getDescription()).isEqualTo("New description");
            assertThat(testProduct.getCategory()).isEqualTo(Category.KIDS);
        }

        @Test
        @DisplayName("should update total stock when no reserved stock")
        void shouldUpdateStockWhenNoReserved() {
            // Given
            testProduct.setReservedStock(0);
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .totalStock(5) // Reducing from 10 to 5
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getTotalStock()).isEqualTo(5);
        }

        @Test
        @DisplayName("should throw BusinessException when reducing stock below reserved")
        void shouldThrowWhenReducingStockBelowReserved() {
            // Given - 10 total, 7 reserved = 3 available, so 7 locked
            testProduct.setReservedStock(7);

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .totalStock(5) // Trying to set to 5, but 7 is locked
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));

            // When/Then
            assertThatThrownBy(() -> productService.updateProduct(100L, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot reduce total stock below currently reserved quantity of 7");
        }

        @Test
        @DisplayName("should allow updating stock equal to reserved")
        void shouldAllowStockEqualToReserved() {
            // Given - 10 total, 7 reserved
            testProduct.setReservedStock(7);

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .totalStock(7) // Setting exactly to reserved amount
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getTotalStock()).isEqualTo(7);
            assertThat(testProduct.getAvailableStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("should allow increasing stock")
        void shouldAllowIncreasingStock() {
            // Given - 10 total, 8 reserved
            testProduct.setReservedStock(8);

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .totalStock(20) // Increasing stock
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getTotalStock()).isEqualTo(20);
            assertThat(testProduct.getAvailableStock()).isEqualTo(12);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFoundForUpdate() {
            // Given
            UpdateProductRequest request = UpdateProductRequest.builder()
                    .name("New Name")
                    .build();

            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.updateProduct(999L, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("should update active status")
        void shouldUpdateActiveStatus() {
            // Given
            testProduct.setActive(true);

            UpdateProductRequest request = UpdateProductRequest.builder()
                    .active(false)
                    .build();

            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.updateProduct(100L, request);

            // Then
            assertThat(testProduct.getActive()).isFalse();
        }
    }

    // =========================================================================
    //                         DELETE PRODUCT TESTS
    // =========================================================================

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProductTests {

        @Test
        @DisplayName("should soft delete product when no orders or reservations")
        void shouldSoftDeleteSuccessfully() {
            // Given
            testProduct.setReservedStock(0);
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(orderQueryService.hasConfirmedOrders(100L)).thenReturn(false);
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);

            // When
            productService.deleteProduct(100L);

            // Then
            assertThat(testProduct.getActive()).isFalse();
            verify(productRepository).save(testProduct);
        }

        @Test
        @DisplayName("should throw BusinessException when confirmed orders exist")
        void shouldThrowWhenConfirmedOrdersExist() {
            // Given
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(orderQueryService.hasConfirmedOrders(100L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete product with confirmed orders");

            // Verify product was not modified
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BusinessException when active reservations exist")
        void shouldThrowWhenActiveReservationsExist() {
            // Given
            testProduct.setReservedStock(3); // Has active reservations
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(orderQueryService.hasConfirmedOrders(100L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Cannot delete product with active reservations");

            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when product not found")
        void shouldThrowWhenProductNotFoundForDelete() {
            // Given
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product not found");
        }

        @Test
        @DisplayName("should check for confirmed orders before reservations")
        void shouldCheckOrdersBeforeReservations() {
            // Given - Product has reserved stock, but we want to verify order check happens first
            testProduct.setReservedStock(5);
            when(productRepository.findById(100L)).thenReturn(Optional.of(testProduct));
            when(orderQueryService.hasConfirmedOrders(100L)).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> productService.deleteProduct(100L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("confirmed orders"); // Should fail on orders, not reservations

            verify(orderQueryService).hasConfirmedOrders(100L);
        }
    }

    // =========================================================================
    //                         PRODUCT ENTITY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Product Entity Logic")
    class ProductEntityTests {

        @Test
        @DisplayName("should calculate available stock correctly")
        void shouldCalculateAvailableStock() {
            // Given
            Product product = TestProductFactory.createProductWithIdAndStock(1L, 100, 35);

            // Then
            assertThat(product.getTotalStock()).isEqualTo(100);
            assertThat(product.getReservedStock()).isEqualTo(35);
            assertThat(product.getAvailableStock()).isEqualTo(65);
        }

        @Test
        @DisplayName("should return true for canReserve when stock available")
        void shouldReturnTrueWhenCanReserve() {
            // Given
            Product product = TestProductFactory.createProductWithIdAndStock(1L, 10, 3);

            // Then
            assertThat(product.canReserve(5)).isTrue(); // 7 available, requesting 5
            assertThat(product.canReserve(7)).isTrue(); // Exactly 7 available
        }

        @Test
        @DisplayName("should return false for canReserve when stock insufficient")
        void shouldReturnFalseWhenCannotReserve() {
            // Given
            Product product = TestProductFactory.createProductWithIdAndStock(1L, 10, 8);

            // Then
            assertThat(product.canReserve(3)).isFalse(); // Only 2 available, requesting 3
        }

        @Test
        @DisplayName("should handle zero available stock")
        void shouldHandleZeroAvailableStock() {
            // Given
            Product product = TestProductFactory.createOutOfStockProduct();

            // Then
            assertThat(product.getAvailableStock()).isEqualTo(0);
            assertThat(product.canReserve(1)).isFalse();
        }
    }
}
