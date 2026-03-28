package com.jk.limited_stock_drop.service;

import com.jk.limited_stock_drop.dto.authorization.request.LoginRequest;
import com.jk.limited_stock_drop.dto.authorization.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    AuthResponse login(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response);

    AuthResponse refreshJwtTokens(HttpServletRequest request, HttpServletResponse response);

    void logout(Long userId, HttpServletResponse response, HttpServletRequest request);
}
