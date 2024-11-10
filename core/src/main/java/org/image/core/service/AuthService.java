package org.image.core.service;

import org.image.core.dto.RegisterReq;
import org.image.core.dto.model.Role;
import org.image.core.dto.UserDto;

public interface AuthService {
    
    boolean login(String userName, String password);
    
    UserDto register(RegisterReq req, Role role);
    
}
