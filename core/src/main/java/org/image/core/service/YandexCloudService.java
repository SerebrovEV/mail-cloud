package org.image.core.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

import static org.image.core.dto.model.TextConstant.TEXT_START_WORK;
import static org.image.core.dto.model.TextConstant.TEXT_UPLOAD_ON_CLOUD;

@Slf4j
@Service
public class YandexCloudService implements CloudService {

    private final AmazonS3 s3;
    private final Integer linkLifetime;
    private final String bucketName;

    public YandexCloudService(@Value("${cloud.link-lifetime}") Integer linkLifetime, @Value("${cloud.accessKey}") String accessKey,
                              @Value("${cloud.secretKey}") String secretKey, @Value("${cloud.bucket-name}") String bucketName,
                              @Value("${cloud.service-endpoint}") String serviceEndpoint,
                              @Value("${cloud.signing-region}") String signingRegion) {
        this.linkLifetime = linkLifetime;
        this.bucketName = bucketName;
        this.s3 = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
    }

    /**
     * Загрузка файлов на облако
     * @param fileName  имя файла для загрузки
     * @param file      файл для загрузки на облако
     * @return результат сохранения в облако в виде true или false
     */
    @Override
    public boolean uploadFile(String fileName, MultipartFile file) {
        log.info(TEXT_START_WORK.formatted("uploadFile"));
        long imageSize = file.getSize();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageSize);
            s3.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        log.info(TEXT_UPLOAD_ON_CLOUD.formatted(fileName));
        return true;
    }

    /**
     * Метод для создания времменной ссылки для скачивания файлов
     * @param imageName - имя файла
     * @return Ссылка на файл
     */
    @Override
    public String createTemporaryLinkForDownload(String imageName) {
        log.info(TEXT_START_WORK.formatted("createTemporaryLinkForDownload"));
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * linkLifetime;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, imageName)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return s3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }
}
