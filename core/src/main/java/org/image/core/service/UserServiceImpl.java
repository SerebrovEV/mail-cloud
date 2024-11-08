package org.image.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.model.Role;
import org.image.core.repository.entity.UserEntity;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.image.core.dto.model.TextConstant.TEXT_NOT_ENOUGH_RIGHT;
import static org.image.core.dto.model.TextConstant.TEXT_USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void blockUserAccount(Long userId, boolean blockValue) {
        if (Role.MODERATOR.equals(getCurrentUser().getRole())) {
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException(TEXT_USER_NOT_FOUND.formatted(userId)));
            userEntity.setAccountNonLocked(blockValue);
            userRepository.save(userEntity);
            log.info("Изменен статус блокировки на %s пользователя %s пользователем %s".formatted(blockValue, userEntity.getEmail(), getCurrentUser().getEmail()));
        } else {
            throw new NotEnoughRightsException(TEXT_NOT_ENOUGH_RIGHT);
        }
    }

    @Override
    public UserEntity getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
    
    @Override
    public UserEntity findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}