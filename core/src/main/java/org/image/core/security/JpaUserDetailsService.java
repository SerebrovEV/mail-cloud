package org.image.core.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.SecurityUserDto;
import org.image.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(u -> {
                    SecurityUser s = new SecurityUser(SecurityUserDto.builder()
                            .email(u.getEmail())
                            .password(u.getPassword())
                            .role(u.getRole())
                            .accountNonLocked(u.isAccountNonLocked())
                            .build());
                    log.info(s.getUsername() + s.getAuthorities().toString());
                    return s;
                })
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь %s не найден ".formatted(email)));
    }

}
