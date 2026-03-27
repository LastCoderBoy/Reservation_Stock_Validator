package com.jk.limited_stock_drop.security.filter;

import com.jk.limited_stock_drop.config.redis.RedisService;
import com.jk.limited_stock_drop.security.CustomUserDetailsService;
import com.jk.limited_stock_drop.security.JwtTokenProcessor;
import com.jk.limited_stock_drop.dto.JwtClaimsPayload;
import com.jk.limited_stock_drop.enums.Role;
import com.jk.limited_stock_drop.exception.InvalidTokenException;
import com.jk.limited_stock_drop.utils.TokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.jk.limited_stock_drop.utils.AppConstants.AUTHORIZATION_HEADER;
import static com.jk.limited_stock_drop.utils.AppConstants.PUBLIC_PATHS;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final JwtTokenProcessor tokenProcessor;
    private final RedisService redisService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        log.info("[JWT-FILTER] Processing request: {} {}", request.getMethod(), path);
        try{
            String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            String rawToken = TokenUtils.validateAndExtractToken(authHeader);

            Optional<JwtClaimsPayload> claimsPayloadOpt = tokenProcessor.validateAndExtractClaims(rawToken);

            if(claimsPayloadOpt.isEmpty()){
                log.warn("[JWT-FILTER] Invalid JWT rawToken");
                throw new InvalidTokenException("Invalid or expired rawToken");
            }

            if(redisService.isTokenBlacklisted(rawToken)){
                log.warn("[JWT-FILTER] Token is blacklisted");
                throw new InvalidTokenException("Token is blacklisted");
            }
            JwtClaimsPayload claimsPayload = claimsPayloadOpt.get();

            Long userId = claimsPayload.userId();
            String username = claimsPayload.username();
            Role userRole = claimsPayload.userRole();

            // UserPrincipal implements UserDetails
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("[JWT-FILTER] Authentication successful for user: {}", username);

            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.warn("[JWT-FILTER] Token error: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("[JWT-FILTER] Unexpected error: {}", e.getMessage());
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + message + "\",\"data\":null}"
        );
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath -> pathMatcher.match(publicPath, path));
    }
}
