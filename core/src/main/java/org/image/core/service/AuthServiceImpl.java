package org.image.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.RegisterReq;
import org.image.core.dto.Role;
import org.image.core.dto.UserDto;
import org.image.core.repository.entity.UserEntity;
import org.image.core.exception.IncorrectFormatEmailException;
import org.image.core.exception.IncorrectPasswordException;
import org.image.core.exception.UserAlreadyCreateException;
import org.image.core.repository.UserRepository;
import org.image.core.util.EmailValidator;
import org.image.core.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    
    @Override
    public boolean login(String userName, String password) {
        UserEntity userEntity = userRepository.findByEmail(userName).orElse(null);
        if (userEntity == null) {
            return false;
        }
        String encryptedPassword = userEntity.getPassword();
        return passwordEncoder.matches(password, encryptedPassword);
    }
    
    @Override
    public UserDto register(RegisterReq req, Role role) {
        boolean result = false;
        if (!PasswordValidator.isValidPassword(req.getPassword())) {
            throw new IncorrectPasswordException("Неверный формат пароля");
        }
        if (!EmailValidator.isValidEmail(req.getEmail())) {
            throw new IncorrectFormatEmailException("Неверный формат email %s".formatted(req.getEmail()));
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new UserAlreadyCreateException("Пользователь с email %s уже существует".formatted(req.getEmail()));
        }
        
        UserEntity userEntity = UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .accountNonLocked(true)
                .role(role)
                .build();
        userRepository.save(userEntity);
        log.info("Создан пользователь id %d email %s".formatted(userEntity.getId(), userEntity.getEmail()));
        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .build();
    }
}
