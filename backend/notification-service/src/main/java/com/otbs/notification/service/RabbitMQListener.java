package com.otbs.notification.service;

import com.otbs.notification.dto.MailRequestDTO;
import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${notification.rabbitmq.leave-request-queue}")
    public void processLeaveRequestNotification(Map<String, Object> message) {
        processNotification(message, "leave-request-service", NotificationType.LEAVE_REQUEST);
    }

    @RabbitListener(queues = "${notification.rabbitmq.medical-visit-queue}")
    public void processMedicalVisitNotification(Map<String, Object> message) {
        processNotification(message, "medical-visit-service", NotificationType.MEDICAL_VISIT);
    }

    @RabbitListener(queues = "${notification.rabbitmq.training-queue}")
    public void processTrainingNotification(Map<String, Object> message) {
        processNotification(message, "training-management-service", NotificationType.TRAINING_SESSION);
    }

    @RabbitListener(queues = "${notification.rabbitmq.mail-queue}")
    public void processMailNotification(Map<String, Object> message) {
        try {
            MailRequestDTO mailRequest = buildMailRequest(message);
            log.info("Sending mail: {}", mailRequest);
            notificationService.sendMail(mailRequest);
        } catch (Exception e) {
            log.error("Error processing mail notification: {}", e.getMessage(), e);
        }
    }

    private void processNotification(Map<String, Object> message, String sender, NotificationType type) {
        log.info("Received {} message: {}", type, message);
        try {
            NotificationRequestDTO notificationRequest = buildNotification(message, sender, type);
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error processing {} notification: {}", type, e.getMessage(), e);
        }
    }

    private NotificationRequestDTO buildNotification(Map<String, Object> message, String sender, NotificationType type) {
        return NotificationRequestDTO.builder()
                .title(getValue(message, "subject"))
                .message(getValue(message, "message"))
                .sender(sender)
                .recipient(getValue(message, "recipient"))
                .type(type)
                .sourceId(getValue(message, "sourceId"))
                .actionUrl(getValue(message, "actionUrl"))
                .build();
    }

    private MailRequestDTO buildMailRequest(Map<String, Object> message) {
        log.info("Received mail message: {}", message);
        return MailRequestDTO.builder()
                .to(getValue(message, "to"))
                .subject(getValue(message, "subject"))
                .body(getValue(message, "body"))
                .build();
    }

    private String getValue(Map<String, Object> message, String key) {
        return Optional.ofNullable(message.get(key)).map(Object::toString).orElse("");
    }
}