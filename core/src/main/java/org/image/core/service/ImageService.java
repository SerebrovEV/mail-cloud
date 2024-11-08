package org.image.core.service;

import org.image.core.dto.ImageDto;
import org.image.core.repository.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ImageService {
    void uploadImage(MultipartFile[] image) throws IOException;
    List<ImageDto> getUserImages(Long imageId, Date date, Long size, String sortBy, String orderBy);
    String downloadUserImage(Long imageId);
    List<ImageDto> getUserImagesForModerator(Long userId, Long imageId, Date date, Long size, String sortBy, String orderBy);
}
