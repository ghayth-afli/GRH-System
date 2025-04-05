package com.otbs.notification.service;

import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.model.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Map;

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
        String requestId = message.get("requestId").toString();
        String employeeName = message.get("employeeName").toString();
        String managerUsername = message.get("managerUsername").toString();
        String leaveType = message.get("leaveType").toString();
        String fromDate = message.get("fromDate").toString();
        String toDate = message.get("toDate").toString();

        return NotificationRequestDTO.builder()
                .title("New Leave Request")
                .message(String.format("%s has requested %s leave from %s to %s",
                        employeeName, leaveType, fromDate, toDate))
                .sender("leave-request-service")
                .recipient(managerUsername)  // Send to manager only
                .type(NotificationType.LEAVE_REQUEST)
                .sourceId(requestId)
                .actionUrl("/leave-requests/" + requestId)
                .build();
    }

    private NotificationRequestDTO buildMedicalVisitNotification(Map<String, Object> message) {
        String visitId = message.get("medicalVisitId").toString();

        return NotificationRequestDTO.builder()
                .title("Medical Visit Available")
                .message(String.format(message.get("message").toString()))
                .sender("medical-visit-service")
                .recipient(message.get("recipient").toString())  // Broadcast to all users
                .type(NotificationType.MEDICAL_VISIT)
                .sourceId(visitId)
                .actionUrl("/medical-visits/" + visitId)
                .build();
    }
}
