package org.image.mail.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.image.mail.model.EventMessage;
import org.image.mail.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static org.image.mail.model.TextConstant.TEXT_PROCESSING_EVENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailListener {

    private final EmailService emailService;

    @RabbitListener(queues = "eventQueue")
    public void receiveMessage(EventMessage eventMessage) {
        log.info(TEXT_PROCESSING_EVENT.formatted(eventMessage.printEventInfo()));
        emailService.sendEmail(eventMessage.getRecipientEmail(), eventMessage.getSubject(), eventMessage.getBody());
    }
}
