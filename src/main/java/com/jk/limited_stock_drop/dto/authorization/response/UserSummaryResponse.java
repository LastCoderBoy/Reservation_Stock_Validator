package com.jk.limited_stock_drop.dto.authorization.response;


import com.jk.limited_stock_drop.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private Role role;
    private boolean active;
}

