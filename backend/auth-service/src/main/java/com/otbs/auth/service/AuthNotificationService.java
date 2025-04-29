package com.otbs.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthNotificationService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.rabbitmq.exchange}")
    private String notificationExchange;

    @Value("${notification.rabbitmq.mail-routing-key}")
    private String mailRoutingKey;

    public void sendMailNotification(String to, String subject, String body) {
        sendNotification(to, subject, body, mailRoutingKey);
    }

    private void sendNotification(String to, String subject, String body, String routingKey) {
        try {
            Map<String, Object> message = Map.of(
                    "recipient", to,
                    "subject", subject,
                    "message", body
            );

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.convertAndSend(notificationExchange, routingKey, message);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }
    }
}