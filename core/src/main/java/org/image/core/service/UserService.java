package org.image.core.service;

import org.image.core.repository.entity.UserEntity;

public interface UserService {
    void blockUserAccount(Long userId, boolean blockValue);

    UserEntity getCurrentUser();
    
    UserEntity findUserById(Long userId);

    UserEntity findUserByEmail(String userEmail);
}
