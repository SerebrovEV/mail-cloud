package org.image.mail.listener;

import lombok.RequiredArgsConstructor;
import org.image.mail.model.EventMessage;
import org.image.mail.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailListener {

    private final EmailService emailService;

    @RabbitListener(queues = "eventQueue")
    public void receiveMessage(EventMessage eventMessage) {
        emailService.sendEmail(eventMessage.getRecipientEmail(), eventMessage.getSubject(), eventMessage.getBody());
    }
}
