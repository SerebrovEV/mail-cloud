package org.image.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginReq {
    @NotNull
    @Email(message = "Некорректный формат email")
    private String email;
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8)
    private String password;
}
