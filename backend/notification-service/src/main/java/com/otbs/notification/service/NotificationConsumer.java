package com.otbs.notification.service;


import com.otbs.common.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final SseService sseService;

    @RabbitListener(queues = "${notification.rabbitmq.events-routing-key}")
    public void processLeaveRequestNotification(Map<String, Object> message) {
        processNotification(message);
    }

    private void processNotification(Map<String, Object> message) {
        log.info("Received RabbitMQ message: {}", message);
        try {
            if (message == null || message.isEmpty()) {
                throw new IllegalArgumentException("Received empty or null message");
            }
            Event event = buildEventNotification(message);
            sseService.broadcast(event);
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
        }
    }

    private Event buildEventNotification(Map<String, Object> message) {
        return Event.builder()
                .eventType(getStringValue(message, "eventType"))
                .entityId(getStringValue(message, "eventId"))
                .entityType(getStringValue(message, "entityType"))
                .payload(getPayloadValue(message))
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String getStringValue(Map<String, Object> message, String key) {
        return Optional.ofNullable(message.get(key))
                .map(Object::toString)
                .orElse("");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getPayloadValue(Map<String, Object> message) {
        return Optional.ofNullable(message.get("payload"))
                .filter(value -> value instanceof Map)
                .map(value -> (Map<String, Object>) value)
                .orElse(new HashMap<>());
    }
}
