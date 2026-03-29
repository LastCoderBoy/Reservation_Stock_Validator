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

        PaginatedResponse<ReservationResponse> response = 
                reservationService.getUserReservations(principal.getId(), filterRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Reservations fetched successfully", response)
        );
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long reservationId,
                                                                               @AuthenticationPrincipal UserPrincipal principal) {
        log.info("[RESERVATION-CONTROLLER] User {} fetching reservation {}", 
                principal.getId(), reservationId);

        ReservationResponse response = reservationService.getReservationById(reservationId, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Reservation fetched successfully", response)
        );
    }

    @PostMapping("/{reservationId}/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(@PathVariable Long reservationId,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        log.info("[RESERVATION-CONTROLLER] User {} checking out reservation {}", 
                principal.getId(), reservationId);

        CheckoutResponse response = reservationService.checkout(reservationId, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Checkout completed successfully", response)
        );
    }

    @PostMapping("/{reservationId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(@PathVariable Long reservationId,
                                                               @AuthenticationPrincipal UserPrincipal principal) {
        log.info("[RESERVATION-CONTROLLER] User {} cancelling reservation {}", 
                principal.getId(), reservationId);

        reservationService.cancelReservation(reservationId, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success("Reservation cancelled successfully")
        );
    }
}
