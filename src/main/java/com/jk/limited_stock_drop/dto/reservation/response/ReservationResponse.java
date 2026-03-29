package com.jk.limited_stock_drop.dto.reservation.response;

import com.jk.limited_stock_drop.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productImageKey;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime expiresAt;
    private Long remainingSeconds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
