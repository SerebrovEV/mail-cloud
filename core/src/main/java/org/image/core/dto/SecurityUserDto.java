package org.image.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUserDto {
    private String email;
    private String password;
    private Role role;
    private boolean accountNonLocked;
}
