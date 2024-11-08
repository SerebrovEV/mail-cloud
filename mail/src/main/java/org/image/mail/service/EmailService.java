package org.image.mail.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final String postServerMail;
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String postServerMail) {
        this.mailSender = mailSender;
        this.postServerMail = postServerMail;
    }
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom(postServerMail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo("89818121788@mail.ru");
//            message.setFrom("evgeniy.smirnov92@gmail.com");
//            helper.setSubject("123");
//            helper.setText("123"); // true для HTML-содержимого
//
//            mailSender.send(message);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
    }

}
