package com.jk.limited_stock_drop.service;

import com.jk.limited_stock_drop.dto.PaginatedResponse;
import com.jk.limited_stock_drop.dto.reservation.request.CreateReservationRequest;
import com.jk.limited_stock_drop.dto.reservation.request.ReservationFilterRequest;
import com.jk.limited_stock_drop.dto.reservation.response.CheckoutResponse;
import com.jk.limited_stock_drop.dto.reservation.response.ReservationResponse;

public interface ReservationService {

    ReservationResponse createReservation(CreateReservationRequest request, Long userId);

    PaginatedResponse<ReservationResponse> getUserReservations(Long userId, ReservationFilterRequest filterRequest);

    ReservationResponse getReservationById(Long reservationId, Long userId);

    CheckoutResponse checkout(Long reservationId, Long userId);

    void cancelReservation(Long reservationId, Long userId);
}
