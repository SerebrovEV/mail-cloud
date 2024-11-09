package org.image.core.service;

import org.image.core.config.RabbitConfig;
import org.image.core.dto.model.Action;
import org.image.core.dto.model.EventMessage;
import org.image.core.dto.model.ImageInfo;
import org.image.core.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.stereotype.Service;

import static org.image.core.dto.model.TextConstant.*;
import static org.image.core.util.ImageUtils.getSizeInMb;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
    private final RabbitTemplate rabbitTemplate;

    public EventServiceImpl(RabbitTemplate rabbitTemplate, Jackson2JsonMessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setMessageConverter(messageConverter);
    }


    public void sendMessage(String userEmail) {
        sendMessageToQueue(userEmail, SUBJECT_REGISTER_USER, TEXT_MESSAGE_REGISTER);
    }

    public void sendMessage(String userEmail, Action action, ImageInfo imageInfo) {
        String subject;
        String message = switch (action) {
            case DOWNLOAD -> {
                subject = SUBJECT_DOWNLOAD_IMAGE;
                yield TEXT_MESSAGE_DOWNLOAD.formatted(imageInfo.successImageNames().stream().findFirst().get(), getSizeInMb(imageInfo));
            }
            case UPLOAD -> {
                subject = SUBJECT_UPLOAD_IMAGE;
                String successUploadImages;
                if (!imageInfo.successImageNames().isEmpty()) {
                    successUploadImages = ImageUtils.getNameImagesList(imageInfo.successImageNames());
                } else {
                    successUploadImages = TEXT_MISSING_IMAGE;
                }
                String errorsUploadImages;
                if (!imageInfo.errorsImageNames().isEmpty()) {
                    errorsUploadImages = ImageUtils.getNameImagesList(imageInfo.errorsImageNames());
                } else {
                    errorsUploadImages = TEXT_MISSING_IMAGE;
                }
                yield TEXT_MESSAGE_UPLOAD.formatted(successUploadImages, errorsUploadImages, getSizeInMb(imageInfo));
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
        log.info(TEXT_ADD_EVENT_TO_QUEUE.formatted(eventMessage.printEventInfo()));
    }
}
