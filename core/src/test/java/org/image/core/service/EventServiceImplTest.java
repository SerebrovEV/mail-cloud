package org.image.core.service;

import org.image.core.config.RabbitConfig;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.EventMessage;
import org.image.core.dto.model.ImageInfo;
import org.image.core.util.ImageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.image.core.dto.model.TextConstant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessageForRegisterUser() {
        String userEmail = "test@example.com";

        eventService.sendMessage(userEmail);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.QUEUE_NAME), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_REGISTER_USER, capturedMessage.getSubject());
        assertEquals(TEXT_MESSAGE_REGISTER, capturedMessage.getBody());
    }

    @Test
    void testSendMessageForDownloadAction() {
        String userEmail = "test@example.com";
        Action action = Action.DOWNLOAD;
        ImageInfo imageInfo = mock(ImageInfo.class);
        when(imageInfo.successImageNames()).thenReturn(List.of("image1.jpg"));
        when(imageInfo.filesSize()).thenReturn(1L);
        when(ImageUtils.getSizeInMb(imageInfo)).thenReturn(1.0);

        eventService.sendMessage(userEmail, action, imageInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.QUEUE_NAME), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_DOWNLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals("Download message: image1.jpg with size 1.5 MB", capturedMessage.getBody());
    }

    @Test
    void testSendMessageForUploadActionWithSuccessAndErrors() {
        String userEmail = "test@example.com";
        Action action = Action.UPLOAD;
        ImageInfo imageInfo = mock(ImageInfo.class);
        when(imageInfo.successImageNames()).thenReturn(List.of("image1.jpg", "image2.jpg"));
        when(imageInfo.errorsImageNames()).thenReturn(List.of("image3.jpg"));
        when(imageInfo.filesSize()).thenReturn(3L);
        when(ImageUtils.getSizeInMb(imageInfo)).thenReturn(3.0);

        String expectedTextMessage = """
                            В хранилище были загружены изображения: image1.jpg, image2.jpg..
                            В хранилище не были загружены изображения: image3.jpg..
                            Общий объём загруженных изображений: 0,00 MB.
                """;
        eventService.sendMessage(userEmail, action, imageInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.QUEUE_NAME), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_UPLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals(expectedTextMessage, capturedMessage.getBody());
    }

    @Test
    void testSendMessageForUploadActionWithNoSuccessImages() {
        String userEmail = "test@example.com";
        Action action = Action.UPLOAD;
        ImageInfo imageInfo = mock(ImageInfo.class);
        when(imageInfo.successImageNames()).thenReturn(List.of());
        when(imageInfo.errorsImageNames()).thenReturn(List.of("image3.jpg"));
        when(imageInfo.filesSize()).thenReturn(0L);
        String expectedTextMessage = """ 
                            В хранилище были загружены изображения: Отсутсвуют.
                            В хранилище не были загружены изображения: image3.jpg..
                            Общий объём загруженных изображений: 0,00 MB.
                """;

        eventService.sendMessage(userEmail, action, imageInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.QUEUE_NAME), messageCaptor.capture());


        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_UPLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals(expectedTextMessage, capturedMessage.getBody());
    }

}