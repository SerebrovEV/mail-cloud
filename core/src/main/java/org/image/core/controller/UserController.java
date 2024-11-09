package org.image.core.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.image.core.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @Operation(
            summary = "Блокировка/разблокировка пользователя",
            description = "Метод для блокировки или разблокировки учетной записи пользователя.",
            tags = {"Пользователи"},
            parameters = {
                    @Parameter(name = "id", description = "ID пользователя", required = true),
                    @Parameter(name = "blockValue", description = "Значение блокировки (true - разблокировать, false - заблокировать)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Учетная запись пользователя успешно обновлена"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
                    @ApiResponse(responseCode = "403", description = "Нет прав для доступа к списку")
            }
    )
    @PostMapping("/{id}/blockUser")
    public ResponseEntity<?> setUserBlock(@PathVariable("id") Long userId,
                                          @RequestParam boolean blockValue) {
        userService.blockUserAccount(userId, blockValue);
        return ResponseEntity.ok().build();
    }
}
