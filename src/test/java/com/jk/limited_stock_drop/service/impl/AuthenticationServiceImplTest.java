package com.jk.limited_stock_drop.service.impl;

import com.jk.limited_stock_drop.config.AuthCookiesManager;
import com.jk.limited_stock_drop.config.redis.RedisService;
import com.jk.limited_stock_drop.config.security.JwtTokenProcessor;
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
import com.jk.limited_stock_drop.service.RefreshTokenService;
import com.jk.limited_stock_drop.utils.TestUserFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static com.jk.limited_stock_drop.utils.AppConstants.AUTHORIZATION_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationServiceImpl Unit Tests")
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProcessor tokenProcessor;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RedisService redisService;

    @Mock
    private AuthCookiesManager cookiesManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpRequest;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User testUser;
    private UserPrincipal testPrincipal;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        testUser = TestUserFactory.createDefaultUser();

        testPrincipal = UserPrincipal.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .isActive(true)
                .userRole(Role.ROLE_USER)
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-value")
                .user(testUser)
                .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
                .revoked(false)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();
    }

    // =========================================================================
    //                              LOGIN TESTS
    // =========================================================================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        @DisplayName("should login successfully and return auth response")
        void shouldLoginSuccessfully() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testPrincipal);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
            when(tokenProcessor.generateAccessToken(eq(1L), eq("testuser"), eq(Role.ROLE_USER)))
                    .thenReturn("access-token-value");
            when(refreshTokenService.createRefreshToken(eq(testUser), anyString(), anyString()))
                    .thenReturn(testRefreshToken);

            // When
            AuthResponse response = authenticationService.login(loginRequest, httpRequest, httpResponse);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token-value");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo("testuser");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(tokenProcessor).generateAccessToken(1L, "testuser", Role.ROLE_USER);
            verify(refreshTokenService).createRefreshToken(eq(testUser), anyString(), anyString());
            verify(cookiesManager).setRefreshTokenCookie(httpResponse, "refresh-token-value");
        }

        @Test
        @DisplayName("should throw BadCredentialsException for invalid credentials")
        void shouldThrowForInvalidCredentials() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("wrongpassword")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When/Then
            assertThatThrownBy(() -> authenticationService.login(loginRequest, httpRequest, httpResponse))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");

            verify(tokenProcessor, never()).generateAccessToken(anyLong(), anyString(), any());
            verify(refreshTokenService, never()).createRefreshToken(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw DisabledException for inactive user")
        void shouldThrowForDisabledUser() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("inactiveuser")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new DisabledException("Account is disabled"));

            // When/Then
            assertThatThrownBy(() -> authenticationService.login(loginRequest, httpRequest, httpResponse))
                    .isInstanceOf(DisabledException.class)
                    .hasMessageContaining("disabled");

            verify(refreshTokenService, never()).createRefreshToken(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found after authentication")
        void shouldThrowWhenUserNotFoundAfterAuth() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testPrincipal);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authenticationService.login(loginRequest, httpRequest, httpResponse))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("should extract client IP from X-Forwarded-For header")
        void shouldExtractClientIpFromHeader() {
            // Given
            LoginRequest loginRequest = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testPrincipal);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 192.168.1.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("TestAgent");
            when(tokenProcessor.generateAccessToken(anyLong(), anyString(), any())).thenReturn("token");
            when(refreshTokenService.createRefreshToken(any(), anyString(), anyString())).thenReturn(testRefreshToken);

            // When
            authenticationService.login(loginRequest, httpRequest, httpResponse);

            // Then
            verify(refreshTokenService).createRefreshToken(eq(testUser), eq("10.0.0.1"), eq("TestAgent"));
        }
    }

    // =========================================================================
    //                         REFRESH JWT TOKENS TESTS
    // =========================================================================

    @Nested
    @DisplayName("refreshJwtTokens()")
    class RefreshJwtTokensTests {

        @Test
        @DisplayName("should refresh tokens successfully")
        void shouldRefreshTokensSuccessfully() {
            // Given
            RefreshToken newRefreshToken = RefreshToken.builder()
                    .id(2L)
                    .token("new-refresh-token")
                    .user(testUser)
                    .expiresAt(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .build();

            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("old-refresh-token"));
            when(httpRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
            when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
            when(refreshTokenService.verifyRefreshToken("old-refresh-token", httpResponse))
                    .thenReturn(testRefreshToken);
            when(refreshTokenService.rotateRefreshToken(eq(testRefreshToken), anyString(), anyString()))
                    .thenReturn(newRefreshToken);
            when(tokenProcessor.generateAccessToken(1L, "testuser", Role.ROLE_USER))
                    .thenReturn("new-access-token");

            // When
            AuthResponse response = authenticationService.refreshJwtTokens(httpRequest, httpResponse);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            assertThat(response.getUser().getUsername()).isEqualTo("testuser");

            verify(refreshTokenService).verifyRefreshToken("old-refresh-token", httpResponse);
            verify(refreshTokenService).rotateRefreshToken(eq(testRefreshToken), anyString(), anyString());
            verify(cookiesManager).setRefreshTokenCookie(httpResponse, "new-refresh-token");
        }

        @Test
        @DisplayName("should throw InvalidTokenException when refresh token not in cookie")
        void shouldThrowWhenRefreshTokenNotFound() {
            // Given
            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authenticationService.refreshJwtTokens(httpRequest, httpResponse))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("Refresh token not found");

            verify(refreshTokenService, never()).verifyRefreshToken(anyString(), any());
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user account is inactive")
        void shouldThrowWhenUserInactive() {
            // Given
            User inactiveUser = TestUserFactory.createInactiveUser();
            RefreshToken tokenWithInactiveUser = RefreshToken.builder()
                    .id(1L)
                    .token("refresh-token")
                    .user(inactiveUser)
                    .expiresAt(Instant.now().plusSeconds(604800))
                    .revoked(false)
                    .build();

            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("refresh-token"));
            when(refreshTokenService.verifyRefreshToken("refresh-token", httpResponse))
                    .thenReturn(tokenWithInactiveUser);

            // When/Then
            assertThatThrownBy(() -> authenticationService.refreshJwtTokens(httpRequest, httpResponse))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("no longer active");

            verify(refreshTokenService, never()).rotateRefreshToken(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("should propagate exception when token verification fails")
        void shouldPropagateVerificationException() {
            // Given
            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("invalid-token"));
            when(refreshTokenService.verifyRefreshToken("invalid-token", httpResponse))
                    .thenThrow(new InvalidTokenException("Token is expired or revoked"));

            // When/Then
            assertThatThrownBy(() -> authenticationService.refreshJwtTokens(httpRequest, httpResponse))
                    .isInstanceOf(InvalidTokenException.class)
                    .hasMessageContaining("expired or revoked");
        }
    }

    // =========================================================================
    //                              LOGOUT TESTS
    // =========================================================================

    @Nested
    @DisplayName("logout()")
    class LogoutTests {

        @Test
        @DisplayName("should logout successfully with all tokens revoked")
        void shouldLogoutSuccessfully() {
            // Given
            String accessToken = "valid-access-token";
            Date futureExpiration = new Date(System.currentTimeMillis() + 900000); // 15 min

            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("refresh-token"));
            when(httpRequest.getHeader(AUTHORIZATION_HEADER))
                    .thenReturn("Bearer " + accessToken);
            when(tokenProcessor.getExpirationDateFromToken(accessToken))
                    .thenReturn(futureExpiration);

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then
            verify(refreshTokenService).revokeRefreshToken("refresh-token");
            verify(redisService).blackListToken(eq(accessToken), anyLong());
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }

        @Test
        @DisplayName("should still clear cookies when refresh token not present")
        void shouldClearCookiesWhenNoRefreshToken() {
            // Given
            String accessToken = "valid-access-token";
            Date futureExpiration = new Date(System.currentTimeMillis() + 900000);

            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.empty());
            when(httpRequest.getHeader(AUTHORIZATION_HEADER))
                    .thenReturn("Bearer " + accessToken);
            when(tokenProcessor.getExpirationDateFromToken(accessToken))
                    .thenReturn(futureExpiration);

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then
            verify(refreshTokenService, never()).revokeRefreshToken(anyString());
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }

        @Test
        @DisplayName("should not blacklist already expired access token")
        void shouldNotBlacklistExpiredToken() {
            // Given
            String accessToken = "expired-access-token";
            Date pastExpiration = new Date(System.currentTimeMillis() - 1000); // Already expired

            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.empty());
            when(httpRequest.getHeader(AUTHORIZATION_HEADER))
                    .thenReturn("Bearer " + accessToken);
            when(tokenProcessor.getExpirationDateFromToken(accessToken))
                    .thenReturn(pastExpiration);

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then
            verify(redisService, never()).blackListToken(anyString(), anyLong());
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }

        @Test
        @DisplayName("should always clear cookies even when exception occurs")
        void shouldAlwaysClearCookiesOnException() {
            // Given
            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("refresh-token"));
            doThrow(new RuntimeException("Redis connection failed"))
                    .when(refreshTokenService).revokeRefreshToken("refresh-token");

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then - cookies should still be cleared despite exception
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }

        @Test
        @DisplayName("should handle missing authorization header gracefully")
        void shouldHandleMissingAuthHeader() {
            // Given
            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("refresh-token"));
            when(httpRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then
            verify(refreshTokenService).revokeRefreshToken("refresh-token");
            verify(redisService, never()).blackListToken(anyString(), anyLong());
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }

        @Test
        @DisplayName("should handle invalid authorization header format")
        void shouldHandleInvalidAuthHeaderFormat() {
            // Given
            when(cookiesManager.extractRefreshTokenFromCookie(httpRequest))
                    .thenReturn(Optional.of("refresh-token"));
            when(httpRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn("InvalidFormat");

            // When
            authenticationService.logout(1L, httpResponse, httpRequest);

            // Then - should not crash, cookies still cleared
            verify(cookiesManager, atLeastOnce()).clearRefreshTokenCookie(httpResponse);
        }
    }
}
