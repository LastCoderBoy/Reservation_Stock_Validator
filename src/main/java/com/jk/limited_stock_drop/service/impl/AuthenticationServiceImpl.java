package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.config.AuthCookiesManager;
import com.jk.limited_stock_drop.config.redis.RedisService;
import com.jk.limited_stock_drop.dto.authorization.request.LoginRequest;
import com.jk.limited_stock_drop.dto.authorization.response.AuthResponse;
import com.jk.limited_stock_drop.entity.RefreshToken;
import com.jk.limited_stock_drop.entity.User;
import com.jk.limited_stock_drop.entity.UserPrincipal;
import com.jk.limited_stock_drop.enums.Role;
import com.jk.limited_stock_drop.exception.InvalidTokenException;
import com.jk.limited_stock_drop.exception.ResourceNotFoundException;
import com.jk.limited_stock_drop.exception.UnauthorizedException;
import com.jk.limited_stock_drop.repository.UserRepository;
import com.jk.limited_stock_drop.config.security.JwtTokenProcessor;
import com.jk.limited_stock_drop.service.AuthenticationService;
import com.jk.limited_stock_drop.service.RefreshTokenService;
import com.jk.limited_stock_drop.utils.HeaderExtractor;
import com.jk.limited_stock_drop.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

import static com.jk.limited_stock_drop.mapper.UserMapper.mapToAuthResponse;
import static com.jk.limited_stock_drop.utils.AppConstants.AUTHORIZATION_HEADER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProcessor tokenProcessor;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;
    private final AuthCookiesManager cookiesManager;

    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        log.info("[AUTH-SERVICE] Login attempt for: {}", loginRequest.getUsername());

        // Spring Security checks in the backend
        // UserPrincipal.isEnabled() -> checks if account is ACTIVE
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("[AUTH-SERVICE] Authentication successful for user: {} (ID: {})",
                principal.getUsername(), principal.getId());

        // Fetch full user entity
        User user = findUserById(principal.getId());

        String clientIp = HeaderExtractor.extractClientIp(httpRequest);
        String userAgent = HeaderExtractor.extractUserAgent(httpRequest);

        // Generate Access Token
        String accessToken = tokenProcessor.generateAccessToken(
                principal.getId(),
                principal.getUsername(),
                principal.getUserRole()
        );

        // Generate Refresh Token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user, clientIp, userAgent
        );

        cookiesManager.setRefreshTokenCookie(httpResponse, refreshToken.getToken());


        return mapToAuthResponse(user, accessToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = cookiesManager.extractRefreshTokenFromCookie(request)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found. Please log in again."));

        // Extract Headers
        String clientIp = HeaderExtractor.extractClientIp(request);
        String userAgent = HeaderExtractor.extractUserAgent(request);

        // verify the old refresh token and create a new one
        RefreshToken storedToken = refreshTokenService.verifyRefreshToken(
                rawRefreshToken,
                response); // method will resolve the Lazy Exception

        User user = storedToken.getUser();
        if (!user.getActive()) {
            throw new UnauthorizedException("Account is no longer active.");
        }

        // Generate new Refresh Token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
                storedToken,
                clientIp,
                userAgent
        );

        // Generate new Access Token
        Long userId = user.getId();
        String username = user.getUsername();
        Role userRole = user.getRole();

        String accessToken = tokenProcessor.generateAccessToken(userId, username, userRole);

        // Set the Refresh token cookie
        cookiesManager.setRefreshTokenCookie(response, newRefreshToken.getToken());

        log.info("[AUTH-SERVICE] Refresh token rotation successful for user: {}", username);

        return mapToAuthResponse(user, accessToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(Long userId, HttpServletResponse response, HttpServletRequest request) {
        try {
            // Revoke Refresh Token
            Optional<String> refreshToken = cookiesManager.extractRefreshTokenFromCookie(request);
            refreshToken.ifPresent(refreshTokenService::revokeRefreshToken);

            // Blacklist the Access Token in Redis
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            blacklistAccessToken(authHeader);

            cookiesManager.clearRefreshTokenCookie(response);
            log.info("[AUTH-SERVICE] Logout successful");

        } catch (ResourceNotFoundException e) {
            log.warn("[AUTH-SERVICE] User not found during logout: {}", e.getMessage());

        } catch (Exception e) {
            log.error("[AUTH-SERVICE] Error during logout for user ID: {}: {}", userId, e.getMessage());

        } finally {
            // ALWAYS clear cookies, regardless of any exception
            cookiesManager.clearRefreshTokenCookie(response);
            log.debug("[AUTH-SERVICE] Refresh token cookie cleared for user ID: {}", userId);
        }
    }

    private void blacklistAccessToken(String authHeader){
        String accessToken = TokenUtils.validateAndExtractToken(authHeader); // might throw InvalidTokenException
        Date tokenExpiration = tokenProcessor.getExpirationDateFromToken(accessToken);
        long remainingTtl = tokenExpiration.getTime() - System.currentTimeMillis();

        if (remainingTtl > 0) {
            redisService.blackListToken(accessToken, remainingTtl);
            log.debug("[AUTH-SERVICE] Access token blacklisted for {}ms", remainingTtl);
        } else {
            log.debug("[AUTH-SERVICE] Access token already expired, skipping blacklist");
        }
    }

    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
}
