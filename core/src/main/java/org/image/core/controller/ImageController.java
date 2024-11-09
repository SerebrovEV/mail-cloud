package org.image.core.controller;


import lombok.RequiredArgsConstructor;
import org.image.core.dto.ImageDto;
import org.image.core.service.ImageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {
    
    private final ImageService imageService;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile[] images) {
            imageService.uploadImage(images);
            return ResponseEntity.ok().build();
    }
    
    @GetMapping("/get")
    public ResponseEntity<List<ImageDto>> getUserImages(@RequestParam(value = "imageId", required = false) Long imageId,
                                                        @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date,
                                                        @RequestParam(value = "size", required = false) Long size,
                                                        @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
                                                        @RequestParam(value = "orderBy", required = false, defaultValue = "asc") String orderBy) {
        List<ImageDto> res = imageService.getUserImages(imageId, date, size, sortBy, orderBy);
        return ResponseEntity.ok(res);
    }
    
    @GetMapping("/getUserImages")
    public ResponseEntity<List<ImageDto>> getUserImagesForModerator(@RequestParam(value = "userId", required = false) Long userId,
                                                                    @RequestParam(value = "imageId", required = false) Long imageId,
                                                                    @RequestParam(value = "date", required = false) LocalDate date,
                                                                    @RequestParam(value = "size", required = false) Long size,
                                                                    @RequestParam(value = "sortBy", required = false, defaultValue = "id") String sortBy,
                                                                    @RequestParam(value = "orderBy", required = false, defaultValue = "asc") String orderBy) {
        List<ImageDto> res = imageService.getUserImagesForModerator(userId, imageId, date, size, sortBy, orderBy);
        return ResponseEntity.ok(res);
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<String> downloadImage(@PathVariable("id") Long imageId) {
        String res = imageService.downloadUserImage(imageId);
        return ResponseEntity.ok(res);
    }
}
