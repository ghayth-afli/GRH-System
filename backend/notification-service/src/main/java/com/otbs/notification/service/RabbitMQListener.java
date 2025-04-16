package com.otbs.notification.service;

import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class RabbitMQListener {

    private final NotificationService notificationService;

    public RabbitMQListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${notification.rabbitmq.leave-request-queue}")
    public void processLeaveRequestNotification(Map<String, Object> message) {
        log.info("Received leave request message: {}", message);

        try {
            NotificationRequestDTO notificationRequest = buildLeaveRequestNotification(message);
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error processing leave request notification: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${notification.rabbitmq.medical-visit-queue}")
    public void processMedicalVisitNotification(Map<String, Object> message) {
        log.info("Received medical visit message: {}", message);

        try {
            NotificationRequestDTO notificationRequest = buildMedicalVisitNotification(message);
            notificationService.createNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Error processing medical visit notification: {}", e.getMessage(), e);
        }
    }

    private NotificationRequestDTO buildLeaveRequestNotification(Map<String, Object> message) {
        String requestId = Optional.ofNullable(message.get("requestId")).map(Object::toString).orElse("");
        String employeeName = Optional.ofNullable(message.get("employeeName")).map(Object::toString).orElse("");
        String managerUsername = Optional.ofNullable(message.get("managerUsername")).map(Object::toString).orElse("");
        String leaveType = Optional.ofNullable(message.get("leaveType")).map(Object::toString).orElse("");
        String fromDate = Optional.ofNullable(message.get("fromDate")).map(Object::toString).orElse("");
        String toDate = Optional.ofNullable(message.get("toDate")).map(Object::toString).orElse("");

        return NotificationRequestDTO.builder()
                .title("New Leave Request")
                .message(String.format("%s has requested %s leave from %s to %s",
                        employeeName, leaveType, fromDate, toDate))
                .sender("leave-request-service")
                .recipient(managerUsername)
                .type(NotificationType.LEAVE_REQUEST)
                .sourceId(requestId)
                .actionUrl("/leave-requests/" + requestId)
                .build();
    }

    private NotificationRequestDTO buildMedicalVisitNotification(Map<String, Object> message) {
        String visitId = Optional.ofNullable(message.get("medicalVisitId")).map(Object::toString).orElse("");

        return NotificationRequestDTO.builder()
                .title("Medical Visit Available")
                .message(Optional.ofNullable(message.get("message")).map(Object::toString).orElse(""))
                .sender("medical-visit-service")
                .recipient(Optional.ofNullable(message.get("recipient")).map(Object::toString).orElse(""))
                .type(NotificationType.MEDICAL_VISIT)
                .sourceId(visitId)
                .actionUrl("/medical-visits/" + visitId)
                .build();
    }
}