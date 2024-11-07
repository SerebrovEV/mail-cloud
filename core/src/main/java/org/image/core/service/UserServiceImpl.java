package org.image.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.Role;
import org.image.core.repository.entity.UserEntity;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void blockUserAccount(Long userId, boolean blockValue) {
        if (Role.MODERATOR.equals(getCurrentUser().getRole())) {
            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь c ID %s не найден ".formatted(userId)));
            userEntity.setAccountNonLocked(blockValue);
            userRepository.save(userEntity);
            log.info("Изменен статус блокировки на %s пользователя %s пользователем %s".formatted(blockValue, userEntity.getEmail(), getCurrentUser().getEmail()));
        } else {
            throw new NotEnoughRightsException("Недостаточно прав для редактирования");
        }
    }

    @Override
    public UserEntity getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).get();
    }
}