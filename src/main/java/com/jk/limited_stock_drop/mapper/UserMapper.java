package com.jk.limited_stock_drop.mapper;

import com.jk.limited_stock_drop.dto.response.AuthResponse;
import com.jk.limited_stock_drop.dto.response.UserSummaryResponse;
import com.jk.limited_stock_drop.entity.User;

import static com.jk.limited_stock_drop.utils.AppConstants.ACCESS_TOKEN_DURATION_MS;

public class UserMapper {

    public static AuthResponse mapToAuthResponse(User user, String accessToken) {

        return AuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(ACCESS_TOKEN_DURATION_MS / 1000)
                .user(mapToUserSummaryResponse(user))
                .build();
    }

    public static UserSummaryResponse mapToUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .role(user.getRole())
                .active(user.getActive())
                .build();
    }
}
