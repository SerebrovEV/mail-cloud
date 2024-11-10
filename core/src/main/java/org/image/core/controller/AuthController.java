package org.image.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.RegisterReq;
import org.image.core.dto.model.Role;
import org.image.core.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Авторизация пользователя",
            description = "Метод для входа пользователя в систему с использованием логина и пароля.",
            tags = {"Авторизация"},
            parameters = {
                    @Parameter(name = "email", description = "Адрес электронной почты пользователя", required = true),
                    @Parameter(name = "password", description = "Пароль пользователя", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam("email") String userName, @RequestParam("password") String password) {
        if (authService.login(userName, password)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }


    @Operation(
            summary = "Регистрация пользователя",
            description = "Метод для регистрации нового пользователя.",
            tags = {"Авторизация"},
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации пользователя",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterReq.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Регистрация прошла успешно"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req) {
        Role role = req.getRole() == null ? Role.USER : req.getRole();
        authService.register(req, role);
        return ResponseEntity.ok().build();
    }
}
