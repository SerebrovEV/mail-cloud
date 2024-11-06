package org.image.core.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageService {
    void uploadImage(MultipartFile image) throws IOException;
}
