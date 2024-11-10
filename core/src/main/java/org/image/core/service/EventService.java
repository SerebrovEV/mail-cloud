package org.image.core.service;

import org.image.core.dto.model.Action;
import org.image.core.dto.model.FileInfo;

public interface EventService {
    void sendMessage(String userEmail);
    void sendMessage(String userEmail, Action action, FileInfo fileInfo);
}
