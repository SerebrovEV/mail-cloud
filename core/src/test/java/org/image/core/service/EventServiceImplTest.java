package org.image.core.service;

import org.image.core.dto.model.Action;
import org.image.core.dto.model.EventMessage;
import org.image.core.dto.model.FileInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import java.util.List;

import static org.image.core.dto.model.TextConstant.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class EventServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private Jackson2JsonMessageConverter messageConverter;

    private final String queueName = "testQueueName";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventService = new EventServiceImpl(rabbitTemplate, messageConverter, queueName);
    }

    @Test
    void testSendMessageF_RegisterUser() {
        String userEmail = "test@example.com";

        eventService.sendMessage(userEmail);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(queueName), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_REGISTER_USER, capturedMessage.getSubject());
        assertEquals(TEXT_MESSAGE_REGISTER, capturedMessage.getBody());
    }

    @Test
    void testSendMessage_DownloadAction() {
        String userEmail = "test@example.com";
        Action action = Action.DOWNLOAD;
        FileInfo fileInfo = new FileInfo(List.of("image1.jpg"), List.of(), 3000000000000L);
        String expectedMessage = "Было скачано изображение image1.jpg и размер 2861022,95 Мб";
        eventService.sendMessage(userEmail, action, fileInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(queueName), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_DOWNLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals(expectedMessage, capturedMessage.getBody());
    }

    @Test
    void testSendMessageFo_UploadActionWithSuccessAndErrors() {
        String userEmail = "test@example.com";
        Action action = Action.UPLOAD;
        FileInfo fileInfo = new FileInfo(List.of("image1.jpg", "image2.jpg"), List.of("image3.jpg"), 3000000000000L);

        String expectedTextMessage = """
                            В хранилище были загружены изображения: image1.jpg, image2.jpg..
                            В хранилище не были загружены изображения: image3.jpg..
                            Общий объём загруженных изображений: 2861022,95 MB.
                """;
        eventService.sendMessage(userEmail, action, fileInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(queueName), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_UPLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals(expectedTextMessage, capturedMessage.getBody());
    }

    @Test
    void testSendMessage_UploadActionWithNoSuccessImages() {
        String userEmail = "test@example.com";
        Action action = Action.UPLOAD;
        FileInfo fileInfo = new FileInfo(List.of(), List.of("image3.jpg"), 3000000000000L);

        String expectedTextMessage = """ 
                            В хранилище были загружены изображения: Отсутсвуют.
                            В хранилище не были загружены изображения: image3.jpg..
                            Общий объём загруженных изображений: 2861022,95 MB.
                """;

        eventService.sendMessage(userEmail, action, fileInfo);

        ArgumentCaptor<EventMessage> messageCaptor = ArgumentCaptor.forClass(EventMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(queueName), messageCaptor.capture());

        EventMessage capturedMessage = messageCaptor.getValue();
        assertEquals(userEmail, capturedMessage.getRecipientEmail());
        assertEquals(SUBJECT_UPLOAD_IMAGE, capturedMessage.getSubject());
        assertEquals(expectedTextMessage, capturedMessage.getBody());
    }

}