package org.image.core.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.image.core.entity.ImageEntity;
import org.image.core.repository.ImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserService userService;
    private final AmazonS3 s3;
    private static final String BUCKET_NAME = null;

    public ImageServiceImpl(ImageRepository imageRepository, UserService userService, AmazonS3 s3) {
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.s3 = AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("https://storage.yandexcloud.net", Regions.US_EAST_1.getName()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        ;
    }

    @Override
    public void uploadImage(MultipartFile image) throws IOException {

        String objectKey = image.getOriginalFilename();
        s3.putObject(new PutObjectRequest(BUCKET_NAME, objectKey, image.getInputStream(), null));

        // Создаем объект FileEntity и сохраняем в базу данных
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setFileName(objectKey);
        imageEntity.setUserEntity(userService.getCurrentUser());
        imageEntity.setYandexUrl("https://storage.yandexcloud.net/" + BUCKET_NAME + "/" + objectKey);
        imageRepository.save(imageEntity);
    }
}
