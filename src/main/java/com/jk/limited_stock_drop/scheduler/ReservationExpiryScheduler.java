package com.jk.limited_stock_drop.scheduler;

import com.jk.limited_stock_drop.entity.InventoryLog;
import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.entity.Reservation;
import com.jk.limited_stock_drop.enums.InventoryAction;
import com.jk.limited_stock_drop.repository.InventoryLogRepository;
import com.jk.limited_stock_drop.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduler responsible for automatically expiring reservations
 * 
 * HOW IT WORKS:
 * -------------
 * 1. Runs every minute (configurable via cron expression)
 * 2. Finds all PENDING reservations where expiresAt < now
 * 3. For each expired reservation:
 *    - Releases the reserved stock back to the product
 *    - Marks reservation status as EXPIRED
 *    - Logs the action to InventoryLog for audit trail
 *
 * WHY EVERY MINUTE?
 * -----------------
 * - Reservations expire after 5 minutes
 * - Checking every minute ensures stock is released promptly
 * - Balance between responsiveness and database load
 * - Can be adjusted to 30 seconds for faster release: "0,30 * * * * ?"
 * 
 * THREAD SAFETY:
 * --------------
 * - Runs in a dedicated thread pool (configured in SchedulingConfig)
 * - Each reservation is processed in its own transaction
 * - If one fails, others continue
 * - Database locks prevent concurrent modifications
 * 
 * @author LastCoderBoy
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;
    private final InventoryLogRepository inventoryLogRepository;

    /**
     * Scheduled task to expire reservations
     * 
     * CRON: "0 *\/1 * * * ?" = Every minute at 0 seconds
     * Can also be configured via application.yaml:
     * app.scheduling.reservation-expiry-cron
     */
    @Scheduled(cron = "${app.scheduling.reservation-expiry-cron}")
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();
        log.debug("[RESERVATION-EXPIRY-SCHEDULER] Starting expiration check at {}", now);

        // Find all expired reservations
        List<Reservation> expiredReservations = reservationRepository.findExpiredReservations(now);

        if (expiredReservations.isEmpty()) {
            log.debug("[RESERVATION-EXPIRY-SCHEDULER] No expired reservations found");
            return;
        }

        log.info("[RESERVATION-EXPIRY-SCHEDULER] Found {} expired reservations to process", 
                expiredReservations.size());

        // Track statistics
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Process each expired reservation
        expiredReservations.forEach(reservation -> {
            try {
                processExpiredReservation(reservation);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
                log.error("[RESERVATION-EXPIRY-SCHEDULER] Failed to expire reservation {}: {}", 
                        reservation.getId(), e.getMessage(), e);
            }
        });

        log.info("[RESERVATION-EXPIRY-SCHEDULER] Expiration completed. Success: {}, Failed: {}", 
                successCount.get(), failureCount.get());
    }

    /**
     * Process a single expired reservation
     * 
     * STEPS:
     * ------
     * 1. Get the product associated with the reservation
     * 2. Calculate stock before release (for audit log)
     * 3. Release the reserved stock back to available pool
     * 4. Mark reservation as EXPIRED
     * 5. Create audit log entry
     * 
     * TRANSACTION:
     * ------------
     * This method is called within the parent @Transactional method,
     * so all operations are atomic. If any step fails, everything rolls back.
     * 
     * @param reservation The reservation to expire
     */
    private void processExpiredReservation(Reservation reservation) {
        log.debug("[RESERVATION-EXPIRY-SCHEDULER] Processing reservation {} for product {} (quantity: {})", 
                reservation.getId(), 
                reservation.getProduct().getId(), 
                reservation.getQuantity());

        Product product = reservation.getProduct();
        int stockBefore = product.getAvailableStock();

        // Release the reserved stock
        product.releaseStock(reservation.getQuantity());

        // Mark reservation as expired
        reservation.expire();

        // Create audit log
        InventoryLog inventoryLog = InventoryLog.builder()
                .product(product)
                .reservation(reservation)
                .action(InventoryAction.STOCK_RELEASED)
                .quantityChange(reservation.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(product.getAvailableStock())
                .description("Stock released from expired reservation (auto-expired by scheduler)")
                .build();
        
        inventoryLogRepository.save(inventoryLog);

        log.info("[RESERVATION-EXPIRY-SCHEDULER] Expired reservation {} - Released {} units of product {} back to available stock", 
                reservation.getId(), 
                reservation.getQuantity(), 
                product.getId());
    }
}
