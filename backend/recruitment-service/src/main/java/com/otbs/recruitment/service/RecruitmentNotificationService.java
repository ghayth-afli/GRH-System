package com.otbs.recruitment.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecruitmentNotificationService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.rabbitmq.exchange}")
    private String notificationExchange;

    @Value("${notification.rabbitmq.recruitment-routing-key}")
    private String recruitmentRoutingKey;

    @Value("${notification.rabbitmq.mail-routing-key}")
    private String mailRoutingKey;

    @PostConstruct
    public void init() {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendRecruitmentNotification(String to, String subject, String body, Long sourceId) {
        sendNotification(to, subject, body, recruitmentRoutingKey, "/recruitment", sourceId);
    }

    public void sendMailNotification(String to, String subject, String body) {
        sendNotification(to, subject, body, mailRoutingKey, null, null);
    }

    private void sendNotification(String to, String subject, String body, String routingKey, String actionUrl, Long sourceId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("recipient", to);
            message.put("subject", subject);
            message.put("message", body);
            if (actionUrl != null) {
                message.put("actionUrl", actionUrl);
            }
            if (sourceId != null) {
                message.put("sourceId", sourceId);
            }
            rabbitTemplate.convertAndSend(notificationExchange, routingKey, message);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }
    }
}