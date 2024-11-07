package org.image.core.controller;

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
    
    @PostMapping("/{id}/blockUser")
    public ResponseEntity<?> setUserBlock(@PathVariable("id") Long userId,
                                          @RequestParam boolean blockValue) {
            userService.blockUserAccount(userId, blockValue);
            return ResponseEntity.ok().build();
    }
}
