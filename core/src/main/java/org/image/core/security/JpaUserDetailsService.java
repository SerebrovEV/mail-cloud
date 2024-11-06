package org.image.core.security;

import lombok.RequiredArgsConstructor;
import org.image.core.dto.SecurityUserDto;
import org.image.core.dto.Role;
import org.image.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(u -> new SecurityUser(SecurityUserDto.builder()
                        .email(u.getEmail())
                        .password(u.getPassword())
                        .role(Role.valueOf(u.getRole()))
                        .accountNonLocked(u.isAccountNonLocked())
                        .build()))
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь %s не найден ".formatted(email)));
    }
    
}
