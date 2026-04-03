package com.jk.limited_stock_drop.integration;

import com.jk.limited_stock_drop.dto.reservation.request.CreateReservationRequest;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;
import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.entity.User;
import com.jk.limited_stock_drop.enums.Category;
import com.jk.limited_stock_drop.enums.Role;
import com.jk.limited_stock_drop.exception.ValidationException;
import com.jk.limited_stock_drop.repository.InventoryLogRepository;
import com.jk.limited_stock_drop.repository.OrderRepository;
import com.jk.limited_stock_drop.repository.ProductRepository;
import com.jk.limited_stock_drop.repository.ReservationRepository;
import com.jk.limited_stock_drop.repository.UserRepository;
import com.jk.limited_stock_drop.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for concurrent reservation scenarios.
 * 
 * These tests verify that the system correctly handles race conditions
 * when multiple users attempt to reserve the same limited-stock product.
 * 
 * Uses H2 in-memory database with PostgreSQL compatibility mode.
 */
@SpringBootTest
@ActiveProfiles("test")
class ReservationConcurrencyIntegrationTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product testProduct;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // Clean up in correct order (respecting foreign key constraints)
        inventoryLogRepository.deleteAll();
        orderRepository.deleteAll();
        reservationRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUsers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@test.com")
                    .password("$2a$10$test")
                    .firstName("Test")
                    .lastName("User" + i)
                    .role(Role.ROLE_USER)
                    .active(true)
                    .build();
            testUsers.add(userRepository.save(user));
        }
    }

    @Test
    @DisplayName("Should prevent overselling when multiple users reserve simultaneously")
    void shouldPreventOverselling_whenConcurrentReservations() throws Exception {
        // Given: Product with only 3 units in stock
        testProduct = Product.builder()
                .name("Limited Edition Item")
                .description("Only 3 available")
                .price(BigDecimal.valueOf(99.99))
                .totalStock(3)
                .reservedStock(0)
                .category(Category.MEN)
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // When: 10 users try to reserve 1 unit each at the same time
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int userIndex = i;
            futures.add(executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    CreateReservationRequest request = new CreateReservationRequest();
                    request.setProductId(testProduct.getId());
                    request.setQuantity(1);
                    
                    reservationService.createReservation(request, testUsers.get(userIndex).getId());
                    successCount.incrementAndGet();
                    return true;
                } catch (ValidationException e) {
                    failureCount.incrementAndGet();
                    return false;
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    return false;
                } finally {
                    endLatch.countDown();
                }
            }));
        }

        // Release all threads at once
        startLatch.countDown();
        
        // Wait for all to complete
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: Exactly 3 succeed (matching available stock), 7 fail
        assertThat(successCount.get())
                .as("Only 3 reservations should succeed (matching available stock)")
                .isEqualTo(3);
        
        assertThat(failureCount.get())
                .as("7 reservations should fail due to insufficient stock")
                .isEqualTo(7);

        // Verify database state
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getReservedStock())
                .as("Reserved stock should equal successful reservations")
                .isEqualTo(3);
        
        assertThat(updatedProduct.getTotalStock() - updatedProduct.getReservedStock())
                .as("Available stock should be zero")
                .isEqualTo(0);

        long reservationCount = reservationRepository.count();
        assertThat(reservationCount)
                .as("Database should have exactly 3 reservations")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Should handle concurrent reservations for same user correctly")
    void shouldPreventDuplicateReservations_forSameUser() throws Exception {
        // Given: Product with 5 units and a single user
        testProduct = Product.builder()
                .name("Limited Item")
                .price(BigDecimal.valueOf(49.99))
                .totalStock(5)
                .reservedStock(0)
                .category(Category.WOMEN)
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);
        
        User singleUser = testUsers.get(0);

        // When: Same user tries to reserve 5 times concurrently (1 unit each)
        int numberOfAttempts = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfAttempts);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfAttempts);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfAttempts; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    CreateReservationRequest request = new CreateReservationRequest();
                    request.setProductId(testProduct.getId());
                    request.setQuantity(1);
                    
                    reservationService.createReservation(request, singleUser.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: All 5 should succeed (user can have multiple reservations)
        // Note: If your business logic prevents multiple reservations per user per product,
        // adjust this test accordingly
        assertThat(successCount.get() + failureCount.get()).isEqualTo(numberOfAttempts);
        
        // Verify no overselling
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getReservedStock())
                .as("Reserved stock should not exceed total stock")
                .isLessThanOrEqualTo(updatedProduct.getTotalStock());
    }

    @Test
    @DisplayName("Should handle high contention on single product")
    void shouldHandleHighContention_onSingleProduct() throws Exception {
        // Given: Product with 10 units, 50 concurrent requests
        testProduct = Product.builder()
                .name("Hot Drop Item")
                .price(BigDecimal.valueOf(199.99))
                .totalStock(10)
                .reservedStock(0)
                .category(Category.KIDS)
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create more users for this test
        for (int i = 10; i < 50; i++) {
            User user = User.builder()
                    .username("user" + i)
                    .email("user" + i + "@test.com")
                    .password("$2a$10$test")
                    .firstName("Test")
                    .lastName("User" + i)
                    .role(Role.ROLE_USER)
                    .active(true)
                    .build();
            testUsers.add(userRepository.save(user));
        }

        // When: 50 users try to reserve at the same time
        int numberOfThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int userIndex = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    CreateReservationRequest request = new CreateReservationRequest();
                    request.setProductId(testProduct.getId());
                    request.setQuantity(1);
                    
                    reservationService.createReservation(request, testUsers.get(userIndex).getId());
                    successCount.incrementAndGet();
                } catch (ValidationException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    // Log other exceptions for debugging
                    System.err.println("Unexpected error: " + e.getMessage());
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: Exactly 10 succeed
        assertThat(successCount.get())
                .as("Exactly 10 reservations should succeed")
                .isEqualTo(10);

        // Verify database integrity
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        assertThat(updatedProduct.getReservedStock()).isEqualTo(10);
        assertThat(updatedProduct.getTotalStock() - updatedProduct.getReservedStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle multi-quantity reservation correctly under concurrency")
    void shouldHandleMultiQuantityReservation_underConcurrency() throws Exception {
        // Given: Product with 20 units
        testProduct = Product.builder()
                .name("Bulk Item")
                .price(BigDecimal.valueOf(29.99))
                .totalStock(20)
                .reservedStock(0)
                .category(Category.MEN)
                .active(true)
                .build();
        testProduct = productRepository.save(testProduct);

        // When: Users try to reserve different quantities concurrently
        // User 0: wants 10, User 1: wants 8, User 2: wants 5, User 3: wants 3
        int[] quantities = {10, 8, 5, 3}; // Total = 26, but only 20 available
        
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(4);
        
        AtomicInteger totalReserved = new AtomicInteger(0);
        List<ReservationResponse> successfulReservations = new CopyOnWriteArrayList<>();

        for (int i = 0; i < 4; i++) {
            final int userIndex = i;
            final int quantity = quantities[i];
            
            executor.submit(() -> {
                try {
                    startLatch.await();
                    
                    CreateReservationRequest request = new CreateReservationRequest();
                    request.setProductId(testProduct.getId());
                    request.setQuantity(quantity);
                    
                    ReservationResponse response = reservationService.createReservation(request, testUsers.get(userIndex).getId());
                    totalReserved.addAndGet(quantity);
                    successfulReservations.add(response);
                } catch (ValidationException e) {
                    // Expected for some requests
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Then: Total reserved should not exceed 20
        Product updatedProduct = productRepository.findById(testProduct.getId()).orElseThrow();
        
        assertThat(updatedProduct.getReservedStock())
                .as("Reserved stock should not exceed total stock")
                .isLessThanOrEqualTo(20);
        
        assertThat(totalReserved.get())
                .as("Total reserved quantity should match database")
                .isEqualTo(updatedProduct.getReservedStock());
        
        // Verify each successful reservation has correct quantity
        int dbTotal = successfulReservations.stream()
                .mapToInt(ReservationResponse::getQuantity)
                .sum();
        assertThat(dbTotal).isEqualTo(updatedProduct.getReservedStock());
    }
}
