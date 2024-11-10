package org.image.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.ImageDto;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.FileInfo;
import org.image.core.dto.model.Role;
import org.image.core.exception.ImageNotFoundException;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.repository.ImageRepository;
import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.image.core.util.ImageSpecification;
import org.image.core.util.ImageValidator;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.image.core.dto.model.TextConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final UserService userService;
    private final CloudService cloudService;
    private final EventService eventService;


    /**
     * Метод загрузки изображений в многопоточном режиме.
     * @param images массив изображений для загрузки
     */
    @Override
    public void uploadImage(MultipartFile[] images) {
        log.info(TEXT_START_WORK.formatted("uploadImage"));
        List<String> successUploadImages = new CopyOnWriteArrayList<>();
        List<String> notUploadImages = new CopyOnWriteArrayList<>();
        UserEntity currentUser = userService.getCurrentUser();
        AtomicLong totalImagesSize = new AtomicLong(0L);
        LocalDateTime uploadDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss");
        String timestamp = uploadDateTime.format(formatter);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch countDownLatch = new CountDownLatch(images.length);
        for (MultipartFile image : images) {
            executorService.submit(() -> {
                String imageName = image.getOriginalFilename();
                if (ImageValidator.isValidImage(imageName)) {
                    String imageNameWithTimestamp = "%s_%s".formatted(timestamp, image.getOriginalFilename());
                    if (cloudService.uploadFile(imageName, image)) {
                        long imageSize = image.getSize();
                        ImageEntity imageEntity = ImageEntity.builder()
                                .fileName(imageNameWithTimestamp)
                                .size(imageSize)
                                .userEntity(currentUser)
                                .uploadDate(uploadDateTime)
                                .build();
                        imageRepository.save(imageEntity);
                        totalImagesSize.addAndGet(imageSize);
                        successUploadImages.add(imageName);
                        log.info(TEXT_SAVED_IMAGE.formatted(imageName));
                        countDownLatch.countDown();
                    } else {
                        notUploadImages.add(imageName);
                        log.info(TEXT_NOT_SAVED_IMAGE.formatted(imageName));
                        countDownLatch.countDown();
                    }
                } else {
                    notUploadImages.add(imageName);
                    log.info(TEXT_INCORRECT_FORMAT_IMAGE.formatted(imageName));
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        } finally {
            executorService.shutdown();
        }
        FileInfo resultFileInfo = new FileInfo(successUploadImages, notUploadImages, totalImagesSize.get());
//        CompletableFuture.runAsync(() -> eventService.sendMessage(currentUser.getEmail(), Action.UPLOAD, resultImageInfo));
        eventService.sendMessage(currentUser.getEmail(), Action.UPLOAD, resultFileInfo);
    }

    /**
     * Метод получения списка загруженных изображений пользователем
     *
     * @param imageId фильтрация по ID изображения
     * @param date    фильтрация по дате
     * @param size    фильтрация по размеру
     * @param sortBy  сортировка по колонке
     * @param orderBy порядок сортировки
     * @return список изображений пользователя
     */
    @Override
    public List<ImageDto> getUserImages(Long imageId, LocalDate date, Long size, String sortBy, String orderBy) {
        log.info(TEXT_START_WORK.formatted("getUserImages"));
        return getUserImages(userService.getCurrentUser(), imageId, date, size, sortBy, orderBy);
    }

    /**
     * Метод получения списка загруженных изображений пользователем для модератора
     *
     * @param userId  фильтрация по ID пользователя
     * @param imageId фильтрация по ID изображения
     * @param date    фильтрация по дате
     * @param size    фильтрация по размеру
     * @param sortBy  сортировка по колонке
     * @param orderBy порядок сортировки
     * @return список изображений пользователя
     */
    @Override
    public List<ImageDto> getUserImagesForModerator(Long userId, Long imageId, LocalDate date, Long size, String sortBy, String orderBy) {
        log.info(TEXT_START_WORK.formatted("getUserImagesForModerator"));
        if (Role.MODERATOR.equals(userService.getCurrentUser().getRole())) {
            UserEntity ownerImages = userId == null ? null : userService.findUserById(userId);
            return getUserImages(ownerImages, imageId, date, size, sortBy, orderBy);
        } else {
            throw new NotEnoughRightsException(TEXT_NOT_ENOUGH_RIGHT);
        }
    }

    /**
     * Метод получения временной ссылки для скачивания изображения
     *
     * @param imageId ID изображения
     * @return ссылка на скачивание
     */
    @Override
    public String downloadUserImage(Long imageId) {
        log.info(TEXT_START_WORK.formatted("downloadUserImage"));
        UserEntity ownerUser = userService.getCurrentUser();
        ImageEntity image = imageRepository.findByIdAndAndUserEntity(imageId, ownerUser)
                .orElseThrow(() -> new ImageNotFoundException(TEXT_IMAGE_NOT_FOUND.formatted(imageId)));
        String temporaryLink = cloudService.createTemporaryLinkForDownload(image.getFileName());
        String userEmail = userService.getCurrentUser().getEmail();
        eventService.sendMessage(userEmail, Action.DOWNLOAD, new FileInfo(List.of(image.getFileName()),
                null, image.getSize()));
        log.info(TEXT_DOWNLOAD_IMAGE.formatted(userEmail, imageId));
        return temporaryLink;
    }

    /**
     * Метод получения списка загруженных изображений пользователем
     *
     * @param userId  фильтрация по ID пользователя
     * @param imageId фильтрация по ID изображения
     * @param date    фильтрация по дате
     * @param size    фильтрация по размеру
     * @param sortBy  сортировка по колонке
     * @param orderBy порядок сортировки
     * @return список изображений пользователя
     */
    private List<ImageDto> getUserImages(UserEntity userId, Long imageId, LocalDate date, Long size, String sortBy, String orderBy) {
        log.info(TEXT_START_WORK.formatted("getUserImages"));
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
}