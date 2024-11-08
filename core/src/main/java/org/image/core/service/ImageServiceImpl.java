package org.image.core.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.image.core.dto.ImageDto;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.ImageInfo;
import org.image.core.dto.model.Role;
import org.image.core.exception.ImageNotFoundException;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.repository.ImageRepository;
import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.image.core.util.ImageSpecification;
import org.image.core.util.ImageValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.image.core.dto.model.TextConstant.TEXT_IMAGE_NOT_FOUND;
import static org.image.core.dto.model.TextConstant.TEXT_NOT_ENOUGH_RIGHT;


@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserService userService;
    private final AmazonS3 s3;
    private final Integer cloudPresignedTime;
    private final EventService eventService;
    private final String bucketName;

    public ImageServiceImpl(ImageRepository imageRepository, UserService userService, EventService eventService,
                            @Value("${cloud.presigned-time}") Integer cloudPresignedTime, @Value("${cloud.accessKey}") String accessKey,
                            @Value("${cloud.secretKey}") String secretKey, @Value("${cloud.bucket-name}") String bucketName,
                            @Value("${cloud.service-endpoint}") String serviceEndpoint,
                            @Value("${cloud.signin-region}") String signingRegion) {
        this.imageRepository = imageRepository;
        this.userService = userService;
        this.eventService = eventService;
        this.cloudPresignedTime = cloudPresignedTime;
        this.bucketName = bucketName;
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }


    @Override
    public void uploadImage(MultipartFile[] images) throws IOException {
        List<String> sucsessUploadFiles = new ArrayList<>();
        List<String> notUploadFiles = new ArrayList<>();
        long totalImageSize = 0L;
        for (MultipartFile image : images) {
            String imageName = image.getOriginalFilename();
            try {
                if (ImageValidator.isValidImage(imageName)) {
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
                    String timestamp = now.format(formatter);
                    String objectKey = "%s_%s".formatted(timestamp, image.getOriginalFilename());
                    s3.putObject(new PutObjectRequest(bucketName, objectKey, image.getInputStream(), null));
                    Long imageSize = image.getSize();
                    ImageEntity imageEntity = ImageEntity.builder()
                            .fileName(objectKey)
                            .size(imageSize)
                            .userEntity(userService.getCurrentUser())
                            .uploadDate(LocalDateTime.now())
                            .build();
                    imageRepository.save(imageEntity);
                    totalImageSize += imageSize;
                    sucsessUploadFiles.add(imageName);
                } else {
                    notUploadFiles.add(imageName);
                }
            } catch (MaxUploadSizeExceededException e) {
                notUploadFiles.add(imageName);
            }
        }
        ImageInfo resultImageInfo = new ImageInfo(sucsessUploadFiles, notUploadFiles, totalImageSize);
        eventService.sendMessage(userService.getCurrentUser().getEmail(), Action.UPLOAD, resultImageInfo);
    }

    @Override
    public List<ImageDto> getUserImages(Long imageId, Date date, Long size, String sortBy, String orderBy) {
        return getUserImages(userService.getCurrentUser(), imageId, date, size, sortBy, orderBy);
    }

    @Override
    public List<ImageDto> getUserImagesForModerator(Long userId, Long imageId, Date date, Long size, String sortBy, String orderBy) {
        if (Role.MODERATOR.equals(userService.getCurrentUser().getRole())) {
            return getUserImages(userService.findUserById(userId), imageId, date, size, sortBy, orderBy);
        } else {
            throw new NotEnoughRightsException(TEXT_NOT_ENOUGH_RIGHT);
        }
    }


    @Override
    public String downloadUserImage(Long imageId) {
        ImageEntity image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException(TEXT_IMAGE_NOT_FOUND.formatted(imageId)));
        String resultLink = generatePresignedUrl(image.getFileName());
        eventService.sendMessage(userService.getCurrentUser().getEmail(), Action.DOWNLOAD, new ImageInfo(List.of(image.getFileName()),
                null, image.getSize()));
        return resultLink;
    }

    private List<ImageDto> getUserImages(UserEntity userId, Long imageId, Date date, Long size, String sortBy, String orderBy) {
        Specification<ImageEntity> spec = Specification.where(ImageSpecification.hasId(imageId))
                .and(ImageSpecification.hasUserId(userId))
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

    private String generatePresignedUrl(String objectKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * cloudPresignedTime; //15 мин
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }
}
