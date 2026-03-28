package com.jk.limited_stock_drop.config.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

import static com.jk.limited_stock_drop.utils.AppConstants.*;

@Component
@Order(1)                   // runs before JwtFilter
@Slf4j
public class RequestMDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            // Populate base MDC — identity starts as guest,
            // JwtFilter will upgrade it to real userId if token is valid
            MDC.put(MDC_REQUEST_ID, UUID.randomUUID().toString().substring(0, 8));
            MDC.put(MDC_METHOD,     request.getMethod());
            MDC.put(MDC_PATH,       request.getRequestURI());
            MDC.put(MDC_USER_ID,    "guest");

            log.info("[REQUEST] Incoming | query='{}'",
                    request.getQueryString() != null ? request.getQueryString() : "");

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[REQUEST] Completed | status={} | duration={}ms",
                    response.getStatus(), duration);

        } finally {
            MDC.clear();    // always clear — thread pools reuse threads
        }
    }
}
