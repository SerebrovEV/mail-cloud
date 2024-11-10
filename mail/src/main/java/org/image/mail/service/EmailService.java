package org.image.mail.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static org.image.mail.model.TextConstant.TEXT_SEND_EMAIL;

@Slf4j
@Service
public class EmailService {

    private final String postServerMail;
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String postServerMail) {
        this.mailSender = mailSender;
        this.postServerMail = postServerMail;
    }

    /**
     * Метод для отправления сообщей пользователям с результатами событий
     * @param to email получателя
     * @param subject тема email
     * @param body текс email
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(postServerMail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info(TEXT_SEND_EMAIL.formatted(to));
    }
}
