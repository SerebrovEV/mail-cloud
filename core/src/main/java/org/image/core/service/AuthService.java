package org.image.core.service;

import org.image.core.dto.RegisterReq;
import org.image.core.dto.Role;

public interface AuthService {
    
    boolean login(String userName, String password);
    
    boolean register(RegisterReq req, Role role);
    
}
