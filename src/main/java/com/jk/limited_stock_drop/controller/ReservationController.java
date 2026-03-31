package com.jk.limited_stock_drop.controller;

import com.jk.limited_stock_drop.dto.ApiResponse;
import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.reservation.request.CreateReservationRequest;
import com.jk.limited_stock_drop.dto.reservation.request.ReservationFilterRequest;
import com.jk.limited_stock_drop.dto.reservation.response.CheckoutResponse;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;
import com.jk.limited_stock_drop.entity.UserPrincipal;
import com.jk.limited_stock_drop.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jk.limited_stock_drop.utils.AppConstants.RESERVATIONS_PATH;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(RESERVATIONS_PATH) // Authentication is required for all methods in this controller
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody CreateReservationRequest request,
                                                                              @AuthenticationPrincipal UserPrincipal principal) {
        log.info("[RESERVATION-CONTROLLER] Creating reservation for product {}", request.getProductId());

        ReservationResponse response = reservationService.createReservation(request, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Reservation created successfully", response)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginatedResponse<ReservationResponse>>> getMyReservations(
            @ModelAttribute @Valid ReservationFilterRequest filterRequest,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.debug("Fetching reservations - User: {} (ID: {}), Status: {}", 
                principal.getUsername(), principal.getId(), filterRequest.getStatus());

        PaginatedResponse<ReservationResponse> response = 
                reservationService.getUserReservations(principal.getId(), filterRequest);
        
        log.debug("Reservations fetched - User: {}, Total: {}, Page: {}/{}", 
                principal.getUsername(), 
                response.getTotalElements(),
                response.getCurrentPage() + 1,
                response.getTotalPages());
        
        return ResponseEntity.ok(
                ApiResponse.success("Reservations fetched successfully", response)
        );
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long reservationId,
                                                                               @AuthenticationPrincipal UserPrincipal principal) {
        log.debug("Fetching reservation - User: {} (ID: {}), Reservation ID: {}", 
                principal.getUsername(), principal.getId(), reservationId);

        ReservationResponse response = reservationService.getReservationById(reservationId, principal.getId());
        
        log.debug("Reservation fetched - ID: {}, Status: {}, Product: '{}', Quantity: {}", 
                response.getId(), response.getStatus(), 
                response.getProductName(), response.getQuantity());
        
        return ResponseEntity.ok(
                ApiResponse.success("Reservation fetched successfully", response)
        );
    }

    @PostMapping("/{reservationId}/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(@PathVariable Long reservationId,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Checkout attempt - User: {} (ID: {}), Reservation ID: {}", 
                principal.getUsername(), principal.getId(), reservationId);

        CheckoutResponse response = reservationService.checkout(reservationId, principal.getId());
        
        log.info("Checkout completed - Order ID: {}, User: {}, Total: {}, Product: '{}', Quantity: {}", 
                response.getOrderId(), principal.getUsername(), response.getTotalPrice(),
                response.getProductName(), response.getQuantity());
        
        return ResponseEntity.ok(
                ApiResponse.success("Checkout completed successfully", response)
        );
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long reservationId,
                                                               @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Cancellation attempt - User: {} (ID: {}), Reservation ID: {}", 
                principal.getUsername(), principal.getId(), reservationId);

        reservationService.cancelReservation(reservationId, principal.getId());
        
        log.info("Reservation cancelled - User: {}, Reservation ID: {}", 
                principal.getUsername(), reservationId);
        
        return ResponseEntity.ok(
                ApiResponse.success("Reservation cancelled successfully")
        );
    }
}
