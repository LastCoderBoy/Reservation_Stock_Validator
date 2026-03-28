package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.config.AuthCookiesManager;
import com.jk.limited_stock_drop.entity.RefreshToken;
import com.jk.limited_stock_drop.entity.User;
import com.jk.limited_stock_drop.exception.InvalidTokenException;
import com.jk.limited_stock_drop.exception.JwtAuthenticationException;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.repository.RefreshTokenRepository;
import com.jk.limited_stock_drop.service.RefreshTokenService;
import com.jk.limited_stock_drop.utils.TokenUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.jk.limited_stock_drop.utils.AppConstants.REFRESH_TOKEN_DURATION_MS;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final AuthCookiesManager cookiesManager;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String clientIP, String userAgent) {
        // Generate a secure random token
        try {
            String tokenString = TokenUtils.generateSecureToken();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token(tokenString)
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(REFRESH_TOKEN_DURATION_MS))
                    .revoked(false)
                    .ipAddress(clientIP)
                    .userAgent(userAgent)
                    .build();

            refreshToken = refreshTokenRepository.save(refreshToken);
            log.info("[REFRESH-TOKEN-SERVICE] Created refresh token for user: {}", user.getId());

            return refreshToken;
        } catch (Exception e) {
            log.error("[REFRESH-TOKEN-SERVICE] Failed to create refresh token: {}", e.getMessage());
            throw new JwtAuthenticationException("Failed to create internal token");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("[REFRESH-TOKEN-SERVICE] Refresh token not found");
                    return new ResourceNotFoundException("Invalid refresh token");
                });
    }

    @Transactional
    @Override
    public RefreshToken verifyRefreshToken(String token, HttpServletResponse response) {
        RefreshToken storedToken = findByToken(token);

        if (!storedToken.isValid()) {
            // Detect possible token reuse attack — revoke everything for this user
            if (storedToken.getRevoked()) {
                log.warn("[AUTH-SERVICE] Refresh token reuse detected for user: {}",
                        storedToken.getUser().getId());
                revokeAllRefreshTokensAsync(storedToken.getUser().getId());
            }
            cookiesManager.clearRefreshTokenCookie(response);
            throw new InvalidTokenException("Refresh token expired. Please log in again.");
        }

        return storedToken;
    }


    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken, String clientIP, String userAgent) {
        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        log.info("[REFRESH-TOKEN-SERVICE] Rotated refresh token for user: {}",
                oldToken.getUser().getId());

        return createRefreshToken(oldToken.getUser(), clientIP, userAgent);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        log.info("[REFRESH-TOKEN-SERVICE] Revoked refresh token for user ID: {}", refreshToken.getUser().getId());
    }

    /**
     * Revoke all refresh tokens for a user
     */
    @Override
    @Async("taskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllRefreshTokensAsync(Long userId) {
        try {
            int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
            log.info("[REFRESH-TOKEN-SERVICE] Revoked {} refresh tokens for user: {}", revokedCount, userId);
        } catch (Exception e) {
            log.error("[REFRESH-TOKEN-SERVICE] Error revoking refresh tokens for user {}: {}",
                    userId, e.getMessage(), e);
        }
    }

    /**
     * Scheduled cleanup job for expired and old revoked tokens
     * Runs daily at 2:00 AM
     * Deletes tokens that are:
     * 1. Expired
     * 2. Revoked more than 7 days ago (keep recent revocations for audit)
     */
    @Override
    @Scheduled(cron = "${app.scheduling.token-cleanup-cron}")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Instant now = Instant.now();
            Instant revokedBeforeTime = now.minus(7, ChronoUnit.DAYS);

            int deletedCount = refreshTokenRepository.deleteExpiredAndRevokedTokens(now, revokedBeforeTime);
            log.info("[REFRESH-TOKEN-SERVICE] Cleaned up {} expired/old revoked tokens", deletedCount);
        } catch (Exception e) {
            log.error("[REFRESH-TOKEN-SERVICE] Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
