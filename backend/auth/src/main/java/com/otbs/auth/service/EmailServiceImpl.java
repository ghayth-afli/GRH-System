package com.otbs.auth.service;

import com.otbs.auth.exception.MailFailedException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EmailServiceImpl implements EmailService {

    private final MailSender mailSender;
    private final SimpleMailMessage templateMessage;

    @Override
    public void sendEmail(String to, String text) {
        SimpleMailMessage message = new SimpleMailMessage(templateMessage);
        message.setTo(to);
        message.setText(text);
        try {
            this.mailSender.send(message);
        }
        catch (MailFailedException e) {
            throw new MailFailedException("Failed to send email");
        }
    }
}
