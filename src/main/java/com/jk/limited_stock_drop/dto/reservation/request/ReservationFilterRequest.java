package com.jk.limited_stock_drop.dto.reservation.request;

import com.jk.limited_stock_drop.enums.ReservationStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import static com.jk.limited_stock_drop.utils.AppConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationFilterRequest {

    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = DEFAULT_PAGE_NUMBER;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = MAX_PAGE_SIZE, message = "Page size cannot exceed " + MAX_PAGE_SIZE)
    @Builder.Default
    private Integer size = DEFAULT_PAGE_SIZE;

    private ReservationStatus status;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDirection = DEFAULT_SORT_DIRECTION;
}
