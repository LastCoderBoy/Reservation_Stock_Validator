package com.jk.limited_stock_drop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jk.limited_stock_drop.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {


    // ==================== Core Identity Fields ====================
    private final Long id;
    private final String username;
    private final String email;

    // ==================== Security Fields ====================
    @JsonIgnore
    private final String password;  // Only used during authentication

    @JsonIgnore
    private final Boolean isActive;

    @JsonIgnore
    private final Role userRole;


    /**
     * Create UserPrincipal from User entity (for login)
     * Used by CustomUserDetailsService
     */
    public static UserPrincipal create(User user) {

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .username(user.getUsername())
                .userRole(user.getRole())
                .isActive(user.getActive())
                .build();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(userRole.name())) ;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    // ==================== Helper Methods ====================

    public String getUserRoleString() {
        return userRole.name();
    }
}