package org.image.core.service;

import lombok.extern.slf4j.Slf4j;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.EventMessage;
import org.image.core.dto.model.FileInfo;
import org.image.core.util.ImageUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static org.image.core.dto.model.TextConstant.*;
import static org.image.core.util.ImageUtils.getSizeInMb;

@Slf4j
@Service
public class EventServiceImpl implements EventService {

    private final RabbitTemplate rabbitTemplate;
    private final String queueName;

    public EventServiceImpl(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter, @Value("${rabbit.queue.name}") String queueName) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
        this.queueName = queueName;
    }

    /**
     * Метод для подготовки события для отправки
     * @param userEmail email пользователя
     */
    public void sendMessage(String userEmail) {
        log.info(TEXT_START_WORK.formatted("sendMessage"));
        sendMessageToQueue(userEmail, SUBJECT_REGISTER_USER, TEXT_MESSAGE_REGISTER);
    }

    /**
     * Метод для подготовки события для отправки
     * @param userEmail email пользователя
     * @param action код события
     * @param fileInfo информация для сообщения
     */
    public void sendMessage(String userEmail, Action action, FileInfo fileInfo) {
        log.info(TEXT_START_WORK.formatted("sendMessage"));
        String subject;
        String message = switch (action) {
            case DOWNLOAD -> {
                subject = SUBJECT_DOWNLOAD_IMAGE;
                yield TEXT_MESSAGE_DOWNLOAD.formatted(fileInfo.successFileNames().stream().findFirst().get(), getSizeInMb(fileInfo));
            }
            case UPLOAD -> {
                subject = SUBJECT_UPLOAD_IMAGE;
                String successUploadFiles;
                if (!fileInfo.successFileNames().isEmpty()) {
                    successUploadFiles = ImageUtils.getNameImagesList(fileInfo.successFileNames());
                } else {
                    successUploadFiles = TEXT_MISSING_IMAGE;
                }
                String errorsUploadFiles;
                if (!fileInfo.errorsFileNames().isEmpty()) {
                    errorsUploadFiles = ImageUtils.getNameImagesList(fileInfo.errorsFileNames());
                } else {
                    errorsUploadFiles = TEXT_MISSING_IMAGE;
                }
                yield TEXT_MESSAGE_UPLOAD.formatted(successUploadFiles, errorsUploadFiles, getSizeInMb(fileInfo));
            }
        };
        sendMessageToQueue(userEmail, subject, message);
    }


    /**
     * Метод отправки сообщения в очередь
     * @param userEmail email пользователя
     * @param subject тема email
     * @param message текст email
     */
    private void sendMessageToQueue(String userEmail, String subject, String message) {
        log.info(TEXT_START_WORK.formatted("sendMessageToQueue"));
        EventMessage eventMessage = EventMessage.builder()
                .recipientEmail(userEmail)
                .subject(subject)
                .body(message)
                .build();
        rabbitTemplate.convertAndSend(queueName, eventMessage);
        log.info(TEXT_ADD_EVENT_TO_QUEUE.formatted(eventMessage.printEventInfo()));
    }
}
