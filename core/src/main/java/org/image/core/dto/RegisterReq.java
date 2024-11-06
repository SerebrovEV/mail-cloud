package org.image.core.dto;

import lombok.Data;

@Data
public class RegisterReq {
    private String email;
    private String password;
    private Role role;
}
