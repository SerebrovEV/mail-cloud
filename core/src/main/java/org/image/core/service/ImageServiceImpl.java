package org.image.core.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.image.core.dto.ImageDto;
import org.image.core.exception.ImageNotFoundException;
import org.image.core.repository.ImageRepository;
import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.image.core.util.ImageSpecification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class ImageServiceImpl implements ImageService {

    private final String accessKey = null;
    private final String secretKey = null;
    private final ImageRepository imageRepository;
    private final UserService userService;
    private final AmazonS3 s3;
    private static final String BUCKET_NAME = "test-mail";

    public ImageServiceImpl(ImageRepository imageRepository, UserService userService) {
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration("https://storage.yandexcloud.net", "ru-central1"))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    @Override
    public void uploadImage(MultipartFile[] images) throws IOException {
        Set<String> notUploadFiles = new HashSet<>();
        for (MultipartFile image : images) {
            try {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                String timestamp = now.format(formatter);
                String objectKey ="%s_%s".formatted(timestamp, image.getOriginalFilename());
                s3.putObject(new PutObjectRequest(BUCKET_NAME, objectKey, image.getInputStream(), null));
                ImageEntity imageEntity = ImageEntity.builder()
                        .fileName(objectKey)
                        .size(image.getSize())
                        .userEntity(userService.getCurrentUser())
                        .uploadDate(LocalDateTime.now())
                        .build();
                imageRepository.save(imageEntity);
            } catch (MaxUploadSizeExceededException e) {
                notUploadFiles.add(image.getName());
            }
        }
    }

    @Override
    public List<ImageDto> getUserImages(Long imageId, Date date, Long size, String sortBy, String orderBy) {
        UserEntity imageOwner = userService.getCurrentUser();
        Specification<ImageEntity> spec = Specification.where(ImageSpecification.hasId(imageId))
                .and(ImageSpecification.hasUserId(imageOwner))
                .and(ImageSpecification.hasDate(date))
                .and(ImageSpecification.hasSize(size));
        Sort sort = Sort.by(Sort.Direction.fromString(orderBy), sortBy);
        return imageRepository.findAll(spec, sort).stream()
                .map(ie -> ImageDto.builder()
                        .id(ie.getId())
                        .fileName(ie.getFileName())
                        .size(ie.getSize())
                        .uploadDate(ie.getUploadDate())
                        .build())
                .toList();
    }

    @Override
    public String downloadUserImage(Long imageId) {
        return imageRepository.findById(imageId).map(ImageEntity::getFileName)
                .map(this::generatePresignedUrl)
                .orElseThrow(() -> new ImageNotFoundException("Изображение с id %s не найдено".formatted(imageId)));
    }


    public String generatePresignedUrl(String objectKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 15; //15 мин
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(BUCKET_NAME, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    public void downloadFileOnDisk(String bucketName, String objectKey, String destinationPath) {
        try {
            S3Object s3Object = s3.getObject(bucketName, objectKey);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            try (FileOutputStream outputStream = new FileOutputStream(destinationPath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            inputStream.close();
            System.out.println("Файл успешно скачан: " + destinationPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}