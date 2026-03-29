package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.reservation.request.CreateReservationRequest;
import com.jk.limited_stock_drop.dto.reservation.request.ReservationFilterRequest;
import com.jk.limited_stock_drop.dto.reservation.response.CheckoutResponse;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;
import com.jk.limited_stock_drop.entity.*;
import com.jk.limited_stock_drop.enums.InventoryAction;
import com.jk.limited_stock_drop.enums.ReservationStatus;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.exception.UnauthorizedException;
import com.jk.limited_stock_drop.exception.ValidationException;
import com.jk.limited_stock_drop.mapper.ReservationMapper;
import com.jk.limited_stock_drop.queryService.ProductQueryService;
import com.jk.limited_stock_drop.repository.InventoryLogRepository;
import com.jk.limited_stock_drop.repository.OrderRepository;
import com.jk.limited_stock_drop.repository.ReservationRepository;
import com.jk.limited_stock_drop.repository.UserRepository;
import com.jk.limited_stock_drop.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ProductQueryService productQueryService;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ReservationResponse createReservation(CreateReservationRequest request, Long userId) {
        log.info("[RESERVATION-SERVICE] Creating reservation for user {} and product {}", userId, request.getProductId());

        User userRef = userRepository.getReferenceById(userId);

        // Lock the product row to prevent concurrent modifications
        Product product = productQueryService.findByIdWithLockOrThrow(request.getProductId());

        // Prevent duplicate reservations
        boolean hasActive = reservationRepository.hasActiveReservation(userId, request.getProductId(), LocalDateTime.now());
        if (hasActive) {
            log.warn("[RESERVATION-SERVICE] User {} already has active reservation for product {}", userId, request.getProductId());
            throw new ValidationException("You already have an active reservation for this product");
        }

        // Check stock availability
        if (!product.canReserve(request.getQuantity())) {
            log.warn("[RESERVATION-SERVICE] Insufficient stock for product {}. Available: {}, Requested: {}",
                    product.getId(), product.getAvailableStock(), request.getQuantity());
            throw new ValidationException("Not enough stock available");
        }

        // Atomically reserve stock at database level
        int stockBefore = product.getAvailableStock();
        product.reserveStock(request.getQuantity());

        // Create reservation
        Reservation reservation = Reservation.builder()
                .product(product)
                .user(userRef)
                .quantity(request.getQuantity())
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        Reservation saved = reservationRepository.save(reservation);

        // Log the inventory change
        InventoryLog inventoryLog = InventoryLog.forReservation(product, saved, stockBefore);
        inventoryLogRepository.save(inventoryLog);

        log.info("[RESERVATION-SERVICE] Reservation {} created successfully for user {}", saved.getId(), userId);
        return ReservationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ReservationResponse> getUserReservations(Long userId, ReservationFilterRequest filterRequest) {
        log.info("[RESERVATION-SERVICE] Fetching reservations for user {} with filters: {}", userId, filterRequest);

        Sort sort = Sort.by(
                filterRequest.getSortDirection().equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                filterRequest.getSortBy()
        );
        Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize(), sort);

        Page<Reservation> reservationPage;
        if (filterRequest.getStatus() != null) {
            reservationPage = reservationRepository.findByUserIdAndStatus(userId, filterRequest.getStatus(), pageable);
        } else {
            reservationPage = reservationRepository.findByUserId(userId, pageable);
        }

        Page<ReservationResponse> responsePage = reservationPage.map(ReservationMapper::toResponse);
        return new PaginatedResponse<>(responsePage);
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationResponse getReservationById(Long reservationId, Long userId) {
        log.info("[RESERVATION-SERVICE] Fetching reservation {} for user {}", reservationId, userId);

        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> {
                    log.error("[RESERVATION-SERVICE] Reservation {} not found or unauthorized for user {}", reservationId, userId);
                    return new ResourceNotFoundException("Reservation not found");
                });

        return ReservationMapper.toResponse(reservation);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CheckoutResponse checkout(Long reservationId, Long userId) {
        log.info("[RESERVATION-SERVICE] Processing checkout for reservation {} by user {}", reservationId, userId);

        Reservation reservation = findByIdOrThrow(reservationId);
        Product product = reservation.getProduct();
        User userRef = userRepository.getReferenceById(userId);

        // Validate reservation ownership and status
        validateReservation(reservation, userId);

        // Mark reservation as confirmed
        reservation.confirm();

        // Deduct from total stock (reserved stock already accounts for it)
        int stockBefore = product.getTotalStock();
        product.setTotalStock(product.getTotalStock() - reservation.getQuantity());
        product.releaseStock(reservation.getQuantity()); // Release from reserved

        // Create order
        Order order = transformToOrder(reservation, product, userRef);
        Order savedOrder = orderRepository.save(order);

        // Log the inventory change
        InventoryLog inventoryLog = InventoryLog.forOrderConfirm(product, savedOrder, stockBefore);
        inventoryLogRepository.save(inventoryLog);

        log.info("[RESERVATION-SERVICE] Checkout completed. Order {} created for reservation {}", savedOrder.getId(), reservationId);
        return ReservationMapper.toCheckoutResponse(savedOrder);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelReservation(Long reservationId, Long userId) {
        log.info("[RESERVATION-SERVICE] Cancelling reservation {} by user {}", reservationId, userId);

        Reservation reservation = findByIdOrThrow(reservationId);
        Product product = reservation.getProduct();

        // Validate ownership and status
        validateReservationForCancellation(reservation, userId);

        // Release the reserved stock
        int stockBefore = product.getAvailableStock();
        product.releaseStock(reservation.getQuantity());

        // Mark reservation as cancelled
        reservation.cancel();

        // Log the inventory change
        InventoryLog inventoryLog = InventoryLog.builder()
                .product(product)
                .reservation(reservation)
                .action(InventoryAction.STOCK_RELEASED)
                .quantityChange(reservation.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(product.getAvailableStock())
                .description("Stock released from cancelled reservation")
                .build();
        inventoryLogRepository.save(inventoryLog);

        log.info("[RESERVATION-SERVICE] Reservation {} cancelled successfully", reservationId);
    }

    // ============================================
    //              HELPER METHODS
    // ============================================

    private Reservation findByIdOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.error("[RESERVATION-SERVICE] Reservation not found with ID: {}", reservationId);
                    return new ResourceNotFoundException("Reservation not found");
                });
    }

    private void validateReservation(Reservation reservation, Long userId) {
        if (!reservation.getUser().getId().equals(userId)) {
            log.error("[RESERVATION-SERVICE] Unauthorized access attempt for reservation {} by user {}", reservation.getId(), userId);
            throw new UnauthorizedException("You are not authorized to access this reservation");
        }

        if (reservation.isExpired()) {
            log.warn("[RESERVATION-SERVICE] Reservation {} has expired", reservation.getId());
            throw new ValidationException("Reservation has expired");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            log.warn("[RESERVATION-SERVICE] Reservation {} is not in PENDING status. Current status: {}", reservation.getId(), reservation.getStatus());
            throw new ValidationException("Reservation is not active");
        }
    }

    private void validateReservationForCancellation(Reservation reservation, Long userId) {
        if (!reservation.getUser().getId().equals(userId)) {
            log.error("[RESERVATION-SERVICE] Unauthorized cancellation attempt for reservation {} by user {}", reservation.getId(), userId);
            throw new UnauthorizedException("You are not authorized to cancel this reservation");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            log.warn("[RESERVATION-SERVICE] Cannot cancel reservation {} with status {}", reservation.getId(), reservation.getStatus());
            throw new ValidationException("Only pending reservations can be cancelled");
        }
    }

    private Order transformToOrder(Reservation reservation, Product product, User user) {
        BigDecimal totalPrice = product.getPrice()
                .multiply(BigDecimal.valueOf(reservation.getQuantity()));

        return Order.builder()
                .reservation(reservation)
                .user(user)
                .product(product)
                .quantity(reservation.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }
}
