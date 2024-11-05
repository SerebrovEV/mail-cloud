package org.image.core.service;

import lombok.RequiredArgsConstructor;
import org.image.core.entity.User;
import org.image.core.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void createUser() {
        userRepository.save(new User());
    }
}
