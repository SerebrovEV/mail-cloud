package org.image.core.service;

import org.image.core.dto.ImageDto;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.FileInfo;
import org.image.core.dto.model.Role;
import org.image.core.exception.NotEnoughRightsException;
import org.image.core.repository.ImageRepository;
import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.image.core.util.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.image.core.dto.model.TextConstant.TEXT_NOT_ENOUGH_RIGHT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ImageServiceImplTest {

    private ImageServiceImpl imageService;
    private ImageRepository imageRepository;
    private UserService userService;
    private CloudService cloudService;
    private EventService eventService;
    private UserEntity currentUser;

    @BeforeEach
    public void setUp() {
        Set<String> permittedFormats = Set.of("jpg", "png", "gif");
        ReflectionTestUtils.setField(ImageValidator.class, "PERMITTED_FORMAT_IMAGES", permittedFormats);
        imageRepository = mock(ImageRepository.class);
        userService = mock(UserService.class);
        eventService = mock(EventService.class);
        cloudService = mock(CloudService.class);
        imageService = new ImageServiceImpl(imageRepository, userService, cloudService, eventService);
        currentUser = new UserEntity(1L, "user@example.com", "123", Role.MODERATOR, true);
        when(userService.getCurrentUser()).thenReturn(currentUser);
    }


    @Test
    public void testUploadImage_Success() {
        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile[] images = {image1};

        when(image1.getOriginalFilename()).thenReturn("image1.jpg");
        when(image1.getSize()).thenReturn(1024L);
        when(cloudService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(true);
        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        imageService.uploadImage(images);

        ArgumentCaptor<ImageEntity> imageEntityCaptor = ArgumentCaptor.forClass(ImageEntity.class);
        verify(imageRepository, times(1)).save(imageEntityCaptor.capture());
        assertEquals(1, imageEntityCaptor.getAllValues().size());

        when(userService.getCurrentUser()).thenReturn(currentUser);
        assertTrue(imageEntityCaptor.getAllValues().getFirst().getFileName().endsWith("image1.jpg"));
        assertEquals(1024L, imageEntityCaptor.getAllValues().getFirst().getSize());
        verify(eventService).sendMessage(eq(currentUser.getEmail()), eq(Action.UPLOAD), any(FileInfo.class));
    }

    @Test
    public void testUploadImage_IncorrectImageName() {

        MultipartFile image1 = mock(MultipartFile.class);
        MultipartFile[] images = {image1};

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(image1.getOriginalFilename()).thenReturn("invalid_image.txt");
        when(cloudService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(false);

        imageService.uploadImage(images);

        verify(imageRepository, never()).save(any(ImageEntity.class));
        verify(eventService).sendMessage(eq(currentUser.getEmail()), eq(Action.UPLOAD), any(FileInfo.class));
    }

    @Test
    void testUploadImage_CloudUploadFailure() {
        MockMultipartFile image = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1024]);

        when(cloudService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(false);

        imageService.uploadImage(new MultipartFile[]{image});

        verify(cloudService).uploadFile(anyString(), any(MultipartFile.class));
        verify(imageRepository, never()).save(any(ImageEntity.class));
        verify(eventService).sendMessage(eq(currentUser.getEmail()), eq(Action.UPLOAD), any(FileInfo.class));
    }

    @Test
    public void testGetUserImages() {

        Long imageId = 1L;
        LocalDate date = LocalDate.now();
        Long size = 10L;
        String sortBy = "date";
        String orderBy = "asc";

        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(1L);
        imageEntity.setFileName("test_image.jpg");
        imageEntity.setSize(1024L);
        imageEntity.setUploadDate(LocalDateTime.now());

        List<ImageEntity> expectedImages = Collections.singletonList(imageEntity);

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(imageRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(expectedImages);

        List<ImageDto> actualImages = imageService.getUserImages(imageId, date, size, sortBy, orderBy);

        assertNotNull(actualImages);
        assertEquals(1, actualImages.size());
        assertEquals(imageEntity.getId(), actualImages.getFirst().getId());
        assertEquals(imageEntity.getFileName(), actualImages.getFirst().getFileName());
        assertEquals(imageEntity.getSize(), actualImages.getFirst().getSize());
        assertEquals(imageEntity.getUploadDate(), actualImages.getFirst().getUploadDate());

        verify(userService).getCurrentUser();
    }

    @Test
    public void testGetUserImagesForModerator_WithModeratorRole() {

        Long userId = 1L;
        Long imageId = 1L;
        LocalDate date = LocalDate.now();
        Long size = 10L;
        String sortBy = "date";
        String orderBy = "asc";
        UserEntity ownerImages = new UserEntity();

        ImageEntity imageEntity = new ImageEntity();

        List<ImageEntity> expectedImages = Collections.singletonList(imageEntity);
        imageEntity.setId(1L);
        imageEntity.setFileName("test_image.jpg");
        imageEntity.setSize(1024L);
        imageEntity.setUploadDate(LocalDateTime.now());

        when(userService.getCurrentUser()).thenReturn(currentUser);
        when(userService.findUserById(userId)).thenReturn(ownerImages);
        when(imageRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(expectedImages);

        List<ImageDto> actualImages = imageService.getUserImagesForModerator(userId, imageId, date, size, sortBy, orderBy);

        assertNotNull(actualImages);
        assertEquals(1, actualImages.size());
        assertEquals(imageEntity.getId(), actualImages.getFirst().getId());
        assertEquals(imageEntity.getFileName(), actualImages.getFirst().getFileName());
        assertEquals(imageEntity.getSize(), actualImages.getFirst().getSize());
        assertEquals(imageEntity.getUploadDate(), actualImages.getFirst().getUploadDate());
    }

    @Test
    public void testGetUserImagesForModerator_WithoutModeratorRole() {
        Long userId = 1L;
        UserEntity testUser = new UserEntity();
        testUser.setRole(Role.USER);

        when(userService.getCurrentUser()).thenReturn(testUser);

        NotEnoughRightsException exception = assertThrows(NotEnoughRightsException.class, () ->
                imageService.getUserImagesForModerator(userId, null, null, null, null, null));

        assertEquals(TEXT_NOT_ENOUGH_RIGHT, exception.getMessage());
        verify(userService).getCurrentUser();
        verify(userService, never()).findUserById(any());
        verify(imageRepository, never()).findAll(any(Specification.class), any(Sort.class));
    }

}