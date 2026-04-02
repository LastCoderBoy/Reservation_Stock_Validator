package com.jk.limited_stock_drop.utils;

import com.jk.limited_stock_drop.entity.User;
import com.jk.limited_stock_drop.enums.Role;

/**
 * Test fixture factory for User entities.
 * Provides pre-configured User objects for unit and integration tests.
 */
public final class TestUserFactory {

    private TestUserFactory() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Creates a standard active user with ROLE_USER.
     */
    public static User createDefaultUser() {
        return User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@example.com")
                .firstName("Test")
                .lastName("User")
                .password("encodedPassword123")
                .active(true)
                .role(Role.ROLE_USER)
                .build();
    }

    /**
     * Creates a user with custom ID.
     */
    public static User createUserWithId(Long id) {
        return User.builder()
                .id(id)
                .username("user" + id)
                .email("user" + id + "@example.com")
                .firstName("User")
                .lastName(String.valueOf(id))
                .password("encodedPassword123")
                .active(true)
                .role(Role.ROLE_USER)
                .build();
    }

    /**
     * Creates a second distinct user for multi-user test scenarios.
     */
    public static User createSecondaryUser() {
        return User.builder()
                .id(2L)
                .username("seconduser")
                .email("seconduser@example.com")
                .firstName("Second")
                .lastName("User")
                .password("encodedPassword456")
                .active(true)
                .role(Role.ROLE_USER)
                .build();
    }

    /**
     * Creates an admin user with ROLE_ADMIN.
     */
    public static User createAdminUser() {
        return User.builder()
                .id(100L)
                .username("adminuser")
                .email("admin@example.com")
                .firstName("Admin")
                .lastName("User")
                .password("adminPassword123")
                .active(true)
                .role(Role.ROLE_ADMIN)
                .build();
    }

    /**
     * Creates an inactive/disabled user.
     */
    public static User createInactiveUser() {
        return User.builder()
                .id(3L)
                .username("inactiveuser")
                .email("inactive@example.com")
                .firstName("Inactive")
                .lastName("User")
                .password("encodedPassword789")
                .active(false)
                .role(Role.ROLE_USER)
                .build();
    }
}
