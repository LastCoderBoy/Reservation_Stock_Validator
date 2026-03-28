package com.jk.limited_stock_drop.service;

import com.jk.limited_stock_drop.entity.RefreshToken;
import com.jk.limited_stock_drop.entity.User;
import jakarta.servlet.http.HttpServletResponse;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String clientIP, String userAgent);

    RefreshToken findByToken(String token);

    RefreshToken verifyRefreshToken(String token, HttpServletResponse response);

    RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokensAsync(Long userId);

    void cleanupExpiredTokens();
}
