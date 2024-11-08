package org.image.core.service;

import org.image.core.repository.UserRepository;
import org.image.core.repository.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.dto.model.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest {
    
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    
    private UserEntity user;
    private UserEntity moderator;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setAccountNonLocked(true);
        
        moderator = new UserEntity();
        moderator.setId(2L);
        moderator.setEmail("moderator@example.com");
        moderator.setRole(Role.MODERATOR);
    }
    
    @Test
    public void testFindUserById_UserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        UserEntity foundUser = userService.findUserById(1L);
        
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    public void testFindUserById_UserDoesNotExist() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        
        UserEntity foundUser = userService.findUserById(2L);
        
        assertNull(foundUser);
        verify(userRepository, times(1)).findById(2L);
    }
    
    @Test
    public void testGetCurrentUser_UserExists() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        

        UserEntity currentUser = userService.getCurrentUser();
        

        assertNotNull(currentUser);
        assertEquals("test@example.com", currentUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
    
    @Test
    public void testGetCurrentUser_UserDoesNotExist() {

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        UserEntity currentUser = userService.getCurrentUser();
        
        assertNull(currentUser);
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }
    
    @Test
    public void testGetCurrentUser_NoAuthentication() {
        SecurityContextHolder.setContext(mock(SecurityContext.class));
        
        UserEntity currentUser = userService.getCurrentUser();
        

        assertNull(currentUser);
    }
    
    @Test
    public void testGetCurrentUser_NotAuthenticated() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        UserEntity currentUser = userService.getCurrentUser();
        
        assertNull(currentUser);
    }
    
    @Test
    public void testBlockUserAccount_Success() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("moderator@example.com");
        when(authentication.getPrincipal()).thenReturn(moderator);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        userService.blockUserAccount(1L, false);
        
        assertFalse(user.isAccountNonLocked());
        verify(userRepository, times(1)).save(user);
    }
    
    @Test
    public void testBlockUserAccount_UserNotFound() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("moderator@example.com");
        when(authentication.getPrincipal()).thenReturn(moderator);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.blockUserAccount(1L, false);
        });
        
        assertEquals("Пользователь c ID 1 не найден ", exception.getMessage());
    }
    
    @Test
    public void testBlockUserAccount_NotModerator() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");
        when(authentication.getPrincipal()).thenReturn(user);
        
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        NotEnoughRightsException exception = assertThrows(NotEnoughRightsException.class, () -> {
            userService.blockUserAccount(1L, false);
        });
        
        assertEquals("Недостаточно прав для редактирования", exception.getMessage());
    }
}