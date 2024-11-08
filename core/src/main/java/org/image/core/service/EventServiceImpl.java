package org.image.core.service;

import org.image.core.config.RabbitConfig;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.EventMessage;
import org.image.core.dto.model.ImageInfo;
import org.image.core.util.ImageNameBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

import static org.image.core.dto.model.TextConstant.SUBJECT_DOWNLOAD_IMAGE;
import static org.image.core.dto.model.TextConstant.SUBJECT_REGISTER_USER;
import static org.image.core.dto.model.TextConstant.SUBJECT_UPLOAD_IMAGE;
import static org.image.core.dto.model.TextConstant.TEXT_MESSAGE_DOWNLOAD;
import static org.image.core.dto.model.TextConstant.TEXT_MESSAGE_REGISTER;
import static org.image.core.dto.model.TextConstant.TEXT_MESSAGE_UPLOAD;

@Service
public class EventServiceImpl implements EventService {
    
    private final RabbitTemplate rabbitTemplate;

    public EventServiceImpl(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }

    
    public void sendMessage(String userEmail) {
        sendMessageToQueue(userEmail,SUBJECT_REGISTER_USER, TEXT_MESSAGE_REGISTER);
    }
    
    public void sendMessage(String userEmail, Action action, ImageInfo imageInfo) {
        String subject;
        String message = switch (action) {
            case DOWNLOAD -> {
                subject = SUBJECT_DOWNLOAD_IMAGE;
                yield TEXT_MESSAGE_DOWNLOAD.formatted(imageInfo.successfulImageNames().stream().findFirst(), imageInfo.filesSize());
            }
            case UPLOAD -> {
                subject = SUBJECT_UPLOAD_IMAGE;
                String successesImages = ImageNameBuilder.buildSuccessImageDownload(imageInfo.successfulImageNames());
                String errorsImages = ImageNameBuilder.buildSuccessImageDownload(imageInfo.errorsImageNames());
                yield  TEXT_MESSAGE_UPLOAD.formatted(successesImages, errorsImages,(double) imageInfo.filesSize() / (1024 * 1024));
            }
        };
        sendMessageToQueue(userEmail, subject, message);
    }
    
    private void sendMessageToQueue(String userEmail, String subject, String message) {
        EventMessage eventMessage = EventMessage.builder()
                .recipientEmail(userEmail)
                .subject(subject)
                .body(message)
                .build();
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, eventMessage);
    }
}
