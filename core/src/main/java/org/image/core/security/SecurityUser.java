package org.image.core.security;

import lombok.AllArgsConstructor;
import org.image.core.dto.SecurityUserDto;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@AllArgsConstructor
public class SecurityUser implements UserDetails {
    
    private SecurityUserDto securityUserDto;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Set.of(
                new SimpleGrantedAuthority("ROLE_" + securityUserDto.getRole().name())
        );
    }
    
    @Override
    public String getPassword() {
        return securityUserDto.getPassword();
    }
    
    @Override
    public String getUsername() {
        return securityUserDto.getEmail();
    }

    @Override
    public boolean isAccountNonLocked() {
        return securityUserDto.isAccountNonLocked();
    }
}