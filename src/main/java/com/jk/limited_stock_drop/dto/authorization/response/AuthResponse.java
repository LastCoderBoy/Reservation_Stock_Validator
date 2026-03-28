package com.jk.limited_stock_drop.dto.authorization.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // in seconds
    private UserSummaryResponse user;
}
