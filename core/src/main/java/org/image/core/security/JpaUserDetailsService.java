package org.image.core.security;

import lombok.RequiredArgsConstructor;
import org.image.core.dto.SecurityUserDto;
import org.image.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static org.image.core.dto.model.TextConstant.TEXT_USER_NOT_FOUND_BY_EMAIL;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(u ->
                        new SecurityUser(SecurityUserDto.builder()
                                .email(u.getEmail())
                                .password(u.getPassword())
                                .role(u.getRole())
                                .accountNonLocked(u.isAccountNonLocked())
                                .build()))
                .orElseThrow(() -> new UsernameNotFoundException(TEXT_USER_NOT_FOUND_BY_EMAIL.formatted(email)));
    }
}