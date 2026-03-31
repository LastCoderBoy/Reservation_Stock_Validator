package com.jk.limited_stock_drop.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.jk.limited_stock_drop.utils.AppConstants.*;

/**
 * Controls which external domains (origins) can make requests to our API from web browsers,
 * preventing unauthorized cross-origin requests while allowing legitimate ones.
 *
 * @author LastCoderBoy
 * @since 1.0.0
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorsConfig {

    private final AppProperties appProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Where requests can come from
        corsConfig.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());
        log.info("[CORS-CONFIG] Allowed origins: {}", appProperties.getCors().getAllowedOrigins());

        // Which HTTP methods are permitted
        corsConfig.setAllowedMethods(appProperties.getCors().getAllowedMethods());

        // ===============================================
        // ALLOW CREDENTIALS (Cookies, Authorization header)
        // ===============================================
        // True since we're:
        // - Using JWT in Authorization header
        // - Using refresh token cookies
        // - Sending any cookies with requests
        corsConfig.setAllowCredentials(appProperties.getCors().isAllowCredentials());

        // ===============================================
        // MAX AGE (Cache preflight response)
        // ===============================================
        // Browser caches preflight response for this duration
        // Reduces OPTIONS requests = better performance
        corsConfig.setMaxAge(appProperties.getCors().getMaxAge());

        // Headers frontend can send
        corsConfig.setAllowedHeaders(Arrays.asList(
                AUTHORIZATION_HEADER,      // "Authorization" - JWT token
                "Content-Type",             // Request body type (application/json)
                "Accept",                   // Response type preference
                "X-Requested-With",         // AJAX request indicator
                "Cache-Control",            // Cache directives
                "Origin",                   // Request origin (automatically sent by browser)
                CORRELATION_ID_HEADER,      // "X-Correlation-ID" - Request tracing
                REQUEST_ID_HEADER           // "X-Request-ID" - Request tracking
        ));
        // NOTE: Do NOT include server-added headers like X-User-Id, X-User-Roles
        // Those are added by backend after authentication, not sent by browser

        // Headers frontend can read
        corsConfig.setExposedHeaders(Arrays.asList(
                AUTHORIZATION_HEADER,      // Frontend can read new JWT from response
                CORRELATION_ID_HEADER,     // For debugging/tracing
                REQUEST_ID_HEADER,         // For debugging/tracing
                USER_ID_HEADER,            // Custom header: logged-in user ID
                USER_ROLES_HEADER          // Custom header: user roles
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);  // Apply to all endpoints

        log.info("[CORS-CONFIG] CORS configuration initialized successfully");
        return source;
    }
}
