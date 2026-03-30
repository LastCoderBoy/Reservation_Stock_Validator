package com.jk.limited_stock_drop.repository;

import com.jk.limited_stock_drop.entity.Reservation;
import com.jk.limited_stock_drop.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Find all reservations for a specific user with optional status filter
     */
    Page<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status, Pageable pageable);

    /**
     * Find all reservations for a specific user (all statuses)
     */
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    /**
     * Find active reservations for a user (excludes expired PENDING ones)
     * This shows only:
     * - PENDING reservations that haven't expired yet
     * - All CONFIRMED, CANCELLED, EXPIRED reservations (historical data)
     */
    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId " +
           "AND (r.status != 'PENDING' OR r.expiresAt > :now)")
    Page<Reservation> findActiveReservationsByUserId(@Param("userId") Long userId,
                                                      @Param("now") LocalDateTime now,
                                                      Pageable pageable);

    /**
     * Find reservation by ID and user ID (for authorization)
     */
    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    /**
     * Find all expired reservations that are still PENDING
     * Used by the scheduler to auto-expire reservations
     */
    @Query("SELECT r FROM Reservation r WHERE r.status = 'PENDING' AND r.expiresAt < :now")
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    /**
     * Check if user has active reservation for a product (to prevent duplicates)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM Reservation r WHERE r.user.id = :userId " +
           "AND r.product.id = :productId " +
           "AND r.status = 'PENDING' " +
           "AND r.expiresAt > :now")
    boolean hasActiveReservation(@Param("userId") Long userId, 
                                  @Param("productId") Long productId, 
                                  @Param("now") LocalDateTime now);
}
