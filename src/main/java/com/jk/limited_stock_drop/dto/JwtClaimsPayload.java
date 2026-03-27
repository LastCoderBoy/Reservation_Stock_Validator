package com.jk.limited_stock_drop.dto;

import com.jk.limited_stock_drop.enums.Role;

public record JwtClaimsPayload(
        Long userId,
        String username,
        Role userRole
) {}
