package com.jk.limited_stock_drop.controller;


import com.jk.limited_stock_drop.dto.ApiResponse;
import com.jk.limited_stock_drop.dto.authorization.request.LoginRequest;
import com.jk.limited_stock_drop.dto.authorization.response.AuthResponse;
import com.jk.limited_stock_drop.entity.UserPrincipal;
import com.jk.limited_stock_drop.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.jk.limited_stock_drop.utils.AppConstants.AUTH_PATH;

/**
 * Authentication Controller
 * Handles user login, logout, and JWT token refresh
 * @author LastCoderBoy
 */
@RestController
@RequestMapping(AUTH_PATH)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                           HttpServletRequest request,
                                                           HttpServletResponse response) {
        log.info("Login attempt - Username: {}", loginRequest.getUsername());

        AuthResponse authResponse = authService.login(loginRequest, request, response);
        return ResponseEntity.ok(ApiResponse.success("User logged in successfully", authResponse));
    }

    // refresh token will be extracted from the cookies
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshJwtToken(HttpServletRequest request,
                                                                     HttpServletResponse response) {
        log.info("Token refresh attempt");
        
        AuthResponse authResponse = authService.refreshJwtTokens(request, response);

        return ResponseEntity.ok(
                ApiResponse.success("JWT tokens refreshed successfully", authResponse)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletResponse response, // used for clearing the cookies
                                                    HttpServletRequest request) {
        log.info("Logout - User ID: {}, Username: {}", 
                principal.getId(), principal.getUsername());

        authService.logout(principal.getId(), response, request);
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully"));
    }

}

