package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.dto.reservation.request.CreateReservationRequest;
import com.jk.limited_stock_drop.dto.reservation.response.CheckoutResponse;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;
import com.jk.limited_stock_drop.entity.*;
import com.jk.limited_stock_drop.enums.ReservationStatus;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.exception.UnauthorizedException;
import com.jk.limited_stock_drop.exception.ValidationException;
import com.jk.limited_stock_drop.queryService.ProductQueryService;
import com.jk.limited_stock_drop.repository.InventoryLogRepository;
import com.jk.limited_stock_drop.repository.OrderRepository;
import com.jk.limited_stock_drop.repository.ReservationRepository;
import com.jk.limited_stock_drop.repository.UserRepository;
import com.jk.limited_stock_drop.utils.TestProductFactory;
import com.jk.limited_stock_drop.utils.TestReservationFactory;
import com.jk.limited_stock_drop.utils.TestUserFactory;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationServiceImpl Unit Tests")
class ReservationServiceImplTest {

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Captor
    private ArgumentCaptor<Reservation> reservationCaptor;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<InventoryLog> inventoryLogCaptor;

    // Test data - initialized from factories
    private User testUser;
    private Product testProduct;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        testUser = TestUserFactory.createDefaultUser();
        testProduct = TestProductFactory.createDefaultProduct();
        testReservation = TestReservationFactory.createPendingReservation(testUser, testProduct);
    }

    // =========================================================================
    //                         CREATE RESERVATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("createReservation()")
    class CreateReservationTests {

        @Test
        @DisplayName("should create reservation successfully when stock is available")
        void shouldCreateReservationSuccessfully() {
            // Given
            Long userId = 1L;
            CreateReservationRequest request = CreateReservationRequest.builder()
                    .productId(100L)
                    .quantity(2)
                    .build();

            // Stubbing
            when(userRepository.getReferenceById(userId)).thenReturn(testUser);
            when(productQueryService.findByIdWithLockOrThrow(100L)).thenReturn(testProduct);
            when(reservationRepository.hasActiveReservation(eq(userId), eq(100L), any(LocalDateTime.class)))
                    .thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
                Reservation saved = invocation.getArgument(0);
                saved.setId(1000L);
                return saved;
            });
            when(inventoryLogRepository.save(any(InventoryLog.class))).thenAnswer(i -> i.getArgument(0));

            // When
            ReservationResponse response = reservationService.createReservation(request, userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getProductId()).isEqualTo(100L);
            assertThat(response.getQuantity()).isEqualTo(2);
            assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);

            // Verify stock was reserved
            assertThat(testProduct.getReservedStock()).isEqualTo(2);
            assertThat(testProduct.getAvailableStock()).isEqualTo(8);

            // Verify inventory log was created
            verify(inventoryLogRepository).save(inventoryLogCaptor.capture());
            InventoryLog log = inventoryLogCaptor.getValue();
            assertThat(log.getQuantityChange()).isEqualTo(2);
            assertThat(log.getStockBefore()).isEqualTo(10);
            assertThat(log.getStockAfter()).isEqualTo(8);
        }

        @Test
        @DisplayName("should throw ValidationException when user already has active reservation")
        void shouldThrowWhenDuplicateReservation() {
            // Given
            Long userId = 1L;
            CreateReservationRequest request = CreateReservationRequest.builder()
                    .productId(100L)
                    .quantity(1)
                    .build();

            when(userRepository.getReferenceById(userId)).thenReturn(testUser);
            when(productQueryService.findByIdWithLockOrThrow(100L)).thenReturn(testProduct);
            when(reservationRepository.hasActiveReservation(eq(userId), eq(100L), any(LocalDateTime.class)))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> reservationService.createReservation(request, userId))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("already have an active reservation");

            // Verify no reservation was saved
            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ValidationException when insufficient stock")
        void shouldThrowWhenInsufficientStock() {
            // Given
            Long userId = 1L;
            CreateReservationRequest request = CreateReservationRequest.builder()
                    .productId(100L)
                    .quantity(15) // More than available (10)
                    .build();

            when(userRepository.getReferenceById(userId)).thenReturn(testUser);
            when(productQueryService.findByIdWithLockOrThrow(100L)).thenReturn(testProduct);
            when(reservationRepository.hasActiveReservation(eq(userId), eq(100L), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> reservationService.createReservation(request, userId))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Not enough stock available");

            verify(reservationRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ValidationException when requesting more than available (partial stock)")
        void shouldThrowWhenPartialStockUnavailable() {
            // Given - Product has 10 total, 7 reserved, only 3 available
            testProduct.setReservedStock(7);

            CreateReservationRequest request = CreateReservationRequest.builder()
                    .productId(100L)
                    .quantity(5) // Requesting 5, but only 3 available
                    .build();

            when(userRepository.getReferenceById(1L)).thenReturn(testUser);
            when(productQueryService.findByIdWithLockOrThrow(100L)).thenReturn(testProduct);
            when(reservationRepository.hasActiveReservation(eq(1L), eq(100L), any(LocalDateTime.class)))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> reservationService.createReservation(request, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Not enough stock available");
        }

        @Test
        @DisplayName("should set expiration time 5 minutes from now")
        void shouldSetCorrectExpirationTime() {
            // Given
            Long userId = 1L;
            CreateReservationRequest request = CreateReservationRequest.builder()
                    .productId(100L)
                    .quantity(1)
                    .build();

            LocalDateTime beforeCreate = LocalDateTime.now();

            when(userRepository.getReferenceById(userId)).thenReturn(testUser);
            when(productQueryService.findByIdWithLockOrThrow(100L)).thenReturn(testProduct);
            when(reservationRepository.hasActiveReservation(anyLong(), anyLong(), any())).thenReturn(false);
            when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> {
                Reservation r = i.getArgument(0);
                r.setId(1L);
                return r;
            });
            when(inventoryLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            reservationService.createReservation(request, userId);

            // Then
            verify(reservationRepository).save(reservationCaptor.capture());
            Reservation saved = reservationCaptor.getValue();

            LocalDateTime afterCreate = LocalDateTime.now();

            // which means between 4 and 6 minutes from now
            assertThat(saved.getExpiresAt())
                    .isAfter(beforeCreate.plusMinutes(4))
                    .isBefore(afterCreate.plusMinutes(6));
        }
    }

    // =========================================================================
    //                           CHECKOUT TESTS
    // =========================================================================

    @Nested
    @DisplayName("checkout()")
    class CheckoutTests {

        @Test
        @DisplayName("should complete checkout successfully")
        void shouldCheckoutSuccessfully() {
            // Given
            testProduct.setReservedStock(2); // Stock was reserved during createReservation

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));
            when(userRepository.getReferenceById(1L)).thenReturn(testUser);
            when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(5000L);
                order.setCreatedAt(LocalDateTime.now());
                return order;
            });
            when(inventoryLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            CheckoutResponse response = reservationService.checkout(1000L, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(5000L);
            assertThat(response.getReservationId()).isEqualTo(1000L);
            assertThat(response.getQuantity()).isEqualTo(2);
            assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(399.98));

            // Verify reservation status changed
            assertThat(testReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

            // Verify stock was deducted from total and released from reserved
            assertThat(testProduct.getTotalStock()).isEqualTo(8);
            assertThat(testProduct.getReservedStock()).isEqualTo(0);

            // Verify order was created with correct data
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder.getQuantity()).isEqualTo(2);
            assertThat(savedOrder.getProduct()).isEqualTo(testProduct);
            assertThat(savedOrder.getUser()).isEqualTo(testUser);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when reservation not found")
        void shouldThrowWhenReservationNotFound() {
            // Given
            when(reservationRepository.findById(9999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> reservationService.checkout(9999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation not found");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own reservation")
        void shouldThrowWhenUnauthorizedCheckout() {
            // Given
            Long differentUserId = 999L;
            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.checkout(1000L, differentUserId))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("should throw ValidationException when reservation is expired")
        void shouldThrowWhenCheckoutExpiredReservation() {
            // Given - Expired reservation
            testReservation.setExpiresAt(LocalDateTime.now().minusMinutes(1));

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.checkout(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should throw ValidationException when reservation is already confirmed")
        void shouldThrowWhenCheckoutAlreadyConfirmed() {
            // Given
            testReservation.setStatus(ReservationStatus.CONFIRMED);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.checkout(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("should throw ValidationException when reservation is cancelled")
        void shouldThrowWhenCheckoutCancelledReservation() {
            // Given
            testReservation.setStatus(ReservationStatus.CANCELLED);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.checkout(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("should calculate total price correctly")
        void shouldCalculateTotalPriceCorrectly() {
            // Given
            testProduct.setPrice(BigDecimal.valueOf(49.99));
            testProduct.setReservedStock(3);
            testReservation.setQuantity(3);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));
            when(userRepository.getReferenceById(1L)).thenReturn(testUser);
            when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
                Order o = i.getArgument(0);
                o.setId(1L);
                o.setCreatedAt(LocalDateTime.now());
                return o;
            });
            when(inventoryLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            CheckoutResponse response = reservationService.checkout(1000L, 1L);

            // Then
            assertThat(response.getTotalPrice()).isEqualByComparingTo(BigDecimal.valueOf(149.97));
        }
    }

    // =========================================================================
    //                       CANCEL RESERVATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("cancelReservation()")
    class CancelReservationTests {

        @Test
        @DisplayName("should cancel reservation and release stock")
        void shouldCancelAndReleaseStock() {
            // Given
            testProduct.setReservedStock(2);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));
            when(inventoryLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            // When
            reservationService.cancelReservation(1000L, 1L);

            // Then
            assertThat(testReservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
            assertThat(testProduct.getReservedStock()).isEqualTo(0);
            assertThat(testProduct.getAvailableStock()).isEqualTo(10);

            // Verify inventory log
            verify(inventoryLogRepository).save(inventoryLogCaptor.capture());
            InventoryLog log = inventoryLogCaptor.getValue();
            assertThat(log.getQuantityChange()).isEqualTo(2);
            assertThat(log.getStockBefore()).isEqualTo(8);
            assertThat(log.getStockAfter()).isEqualTo(10);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when reservation not found")
        void shouldThrowWhenReservationNotFound() {
            // Given
            when(reservationRepository.findById(9999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> reservationService.cancelReservation(9999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation not found");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user doesn't own reservation")
        void shouldThrowWhenUnauthorizedCancel() {
            // Given
            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.cancelReservation(1000L, 999L))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("should throw ValidationException when cancelling non-pending reservation")
        void shouldThrowWhenCancellingConfirmedReservation() {
            // Given
            testReservation.setStatus(ReservationStatus.CONFIRMED);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.cancelReservation(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Only pending reservations can be cancelled");
        }

        @Test
        @DisplayName("should throw ValidationException when cancelling already cancelled reservation")
        void shouldThrowWhenCancellingAlreadyCancelled() {
            // Given
            testReservation.setStatus(ReservationStatus.CANCELLED);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.cancelReservation(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Only pending reservations can be cancelled");
        }

        @Test
        @DisplayName("should throw ValidationException when cancelling expired reservation")
        void shouldThrowWhenCancellingExpiredReservation() {
            // Given
            testReservation.setStatus(ReservationStatus.EXPIRED);

            when(reservationRepository.findById(1000L)).thenReturn(Optional.of(testReservation));

            // When/Then
            assertThatThrownBy(() -> reservationService.cancelReservation(1000L, 1L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Only pending reservations can be cancelled");
        }
    }

    // =========================================================================
    //                      GET RESERVATION BY ID TESTS
    // =========================================================================

    @Nested
    @DisplayName("getReservationById()")
    class GetReservationByIdTests {

        @Test
        @DisplayName("should return reservation for valid user and id")
        void shouldReturnReservation() {
            // Given
            when(reservationRepository.findByIdAndUserId(1000L, 1L))
                    .thenReturn(Optional.of(testReservation));

            // When
            ReservationResponse response = reservationService.getReservationById(1000L, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1000L);
            assertThat(response.getProductId()).isEqualTo(100L);
            assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when reservation not found")
        void shouldThrowWhenNotFound() {
            // Given
            when(reservationRepository.findByIdAndUserId(9999L, 1L))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> reservationService.getReservationById(9999L, 1L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation not found");
        }

        @Test
        @DisplayName("should throw when user tries to access another user's reservation")
        void shouldThrowWhenAccessingOtherUserReservation() {
            // Given - Repository returns empty when userId doesn't match
            when(reservationRepository.findByIdAndUserId(1000L, 999L))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> reservationService.getReservationById(1000L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    //                         STOCK MANAGEMENT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Stock Management")
    class StockManagementTests {

        @Test
        @DisplayName("should correctly track available stock after multiple operations")
        void shouldTrackStockCorrectly() {
            // Given - Product starts with 10 total, 0 reserved
            assertThat(testProduct.getAvailableStock()).isEqualTo(10);

            // When - Reserve 3
            testProduct.reserveStock(3);
            assertThat(testProduct.getAvailableStock()).isEqualTo(7);
            assertThat(testProduct.getReservedStock()).isEqualTo(3);

            // When - Reserve 2 more
            testProduct.reserveStock(2);
            assertThat(testProduct.getAvailableStock()).isEqualTo(5);
            assertThat(testProduct.getReservedStock()).isEqualTo(5);

            // When - Release 3 (cancellation)
            testProduct.releaseStock(3);
            assertThat(testProduct.getAvailableStock()).isEqualTo(8);
            assertThat(testProduct.getReservedStock()).isEqualTo(2);
        }

        @Test
        @DisplayName("should throw when trying to reserve more than available")
        void shouldThrowWhenReservingTooMuch() {
            // Given
            testProduct.setReservedStock(8); // Only 2 remaining in the stock

            // When/Then
            assertThatThrownBy(() -> testProduct.reserveStock(5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Insufficient stock");
        }

        @Test
        @DisplayName("should throw when trying to release more than reserved")
        void shouldThrowWhenReleasingTooMuch() {
            // Given
            testProduct.setReservedStock(2);

            // When/Then
            assertThatThrownBy(() -> testProduct.releaseStock(5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot release more");
        }
    }

    // =========================================================================
    //                      RESERVATION STATUS TESTS
    // =========================================================================

    @Nested
    @DisplayName("Reservation Status Transitions")
    class ReservationStatusTests {

        @Test
        @DisplayName("should correctly identify expired reservation")
        void shouldIdentifyExpiredReservation() {
            // Given
            testReservation.setExpiresAt(LocalDateTime.now().minusSeconds(1));

            // Then
            assertThat(testReservation.isExpired()).isTrue();
            assertThat(testReservation.isActive()).isFalse();
        }

        @Test
        @DisplayName("should correctly identify active reservation")
        void shouldIdentifyActiveReservation() {
            // Given
            testReservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            testReservation.setStatus(ReservationStatus.PENDING);

            // Then
            assertThat(testReservation.isExpired()).isFalse();
            assertThat(testReservation.isActive()).isTrue();
        }

        @Test
        @DisplayName("should not be active when confirmed even if not expired")
        void shouldNotBeActiveWhenConfirmed() {
            // Given
            testReservation.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            testReservation.setStatus(ReservationStatus.CONFIRMED);

            // Then
            assertThat(testReservation.isActive()).isFalse();
        }

        @Test
        @DisplayName("should calculate remaining seconds correctly")
        void shouldCalculateRemainingSeconds() {
            // Given
            testReservation.setExpiresAt(LocalDateTime.now().plusSeconds(120));

            // Then
            long remaining = testReservation.getRemainingSeconds();
            assertThat(remaining).isBetween(118L, 121L);
        }

        @Test
        @DisplayName("should return 0 remaining seconds for expired reservation")
        void shouldReturnZeroForExpired() {
            // Given
            testReservation.setExpiresAt(LocalDateTime.now().minusSeconds(10));

            // Then
            assertThat(testReservation.getRemainingSeconds()).isZero();
        }
    }
}
