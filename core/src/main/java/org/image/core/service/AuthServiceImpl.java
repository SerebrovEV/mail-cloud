package org.image.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.RegisterReq;
import org.image.core.dto.model.Role;
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

import static org.image.core.dto.model.TextConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final EventService eventService;
    
    
    @Override
    public boolean login(String userName, String password) {
        UserEntity userEntity = userRepository.findByEmail(userName).orElse(null);
        if (userEntity == null) {
            return false;
        }
        return passwordEncoder.matches(password, userEntity.getPassword());
    }
    
    @Override
    public UserDto register(RegisterReq req, Role role) {
        if (!PasswordValidator.isValidPassword(req.getPassword())) {
            throw new IncorrectPasswordException(TEXT_INCORRECT_FORMAT_PASSWORD);
        }
        if (!EmailValidator.isValidEmail(req.getEmail())) {
            throw new IncorrectFormatEmailException(TEXT_INCORRECT_FORMAT_EMAIL.formatted(req.getEmail()));
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new UserAlreadyCreateException(TEXT_EMAIL_ALREADY_EXIST.formatted(req.getEmail()));
        }
        
        UserEntity userEntity = UserEntity.builder()
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .accountNonLocked(true)
                .role(role)
                .build();
        userRepository.save(userEntity);
        log.info(TEXT_CREATE_USER.formatted(userEntity.getId(), userEntity.getEmail()));
        eventService.sendMessage(userEntity.getEmail());
        return UserDto.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .build();
    }
}
