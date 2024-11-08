package org.image.core.dto;

import lombok.Data;
import org.image.core.dto.model.Role;

@Data
public class RegisterReq {
    private String email;
    private String password;
    private Role role;
}
