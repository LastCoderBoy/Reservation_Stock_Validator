package com.jk.limited_stock_drop.utils;

import java.util.List;

public final class AppConstants {

    public static final String API_VERSION = "v1";
    public static final String BASE_PATH = "/api/" + API_VERSION;
    public static final String AUTH_PATH = BASE_PATH + "/auth";
    public static final String ORDER_PATH = BASE_PATH + "/order";
    public static final String PRODUCT_CATALOG_PATH = BASE_PATH + "/products";

    public static final List<String> PUBLIC_PATHS = List.of(
            AUTH_PATH + "/login",
            AUTH_PATH + "/refresh-token",

            // TODO: Actuator endpoints might be private
            "/actuator/health",
            "/actuator/info",

            // Swagger/API docs
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**"
    );


    // ========== HTTP Headers ==========
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String IP_ADDRESS_HEADER = "X-Forwarded-For";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String USER_ROLES_HEADER = "X-User-Roles";
    public static final String USER_EMAIL_HEADER = "X-User-Email";

    // ========== JWT ==========
    public static final String BEARER_PREFIX = "Bearer ";
    public static final int BEARER_PREFIX_LENGTH = 7;
    public static final long ACCESS_TOKEN_DURATION_MS = 15 * 60 * 1000; // 15 minutes
    public static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000; // 7 days
    public static final String JWT_CLAIM_USER_ID = "userId";
    public static final String JWT_CLAIM_ROLES = "userRole";
    public static final String JWT_CLAIM_EMAIL = "email";
    public static final String JWT_CLAIM_TOKEN_TYPE = "tokenType";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";


    // ========== Cache Keys ==========
    public static final String CACHE_USER_PROFILE_PREFIX = "auth:user:profile:";
    public static final String CACHE_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:token:";
    public static final String CACHE_REFRESH_TOKEN_PREFIX = "auth:refresh:token:";
    public static final String CACHE_OTP_PREFIX = "auth:otp:";
    public static final String CACHE_SESSION_PREFIX = "session:";
}
