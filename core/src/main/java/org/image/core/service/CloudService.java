package org.image.core.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudService {

    boolean uploadFile(String fileName, MultipartFile file);
    String createTemporaryLinkForDownload(String imageName);
}
