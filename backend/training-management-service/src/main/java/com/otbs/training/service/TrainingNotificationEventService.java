package com.otbs.training.service;

import com.otbs.common.event.Event;
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
public class TrainingNotificationEventService {
    private final RabbitTemplate rabbitTemplate;

    @Value("${notification.rabbitmq.exchange}")
    private String notificationExchange;

    @Value("${notification.rabbitmq.events-routing-key}")
    private String eventRoutingKey;

    @PostConstruct
    public void init() {
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    public void sendEventNotification(Event event) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", event.getEventType());
            message.put("eventId", event.getEntityId());
            message.put("EntityType", event.getEntityType());
            message.put("timestamp", event.getTimestamp());
            message.put("payload", event.getPayload());
            rabbitTemplate.convertAndSend(notificationExchange, eventRoutingKey, message);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
        }
    }
}