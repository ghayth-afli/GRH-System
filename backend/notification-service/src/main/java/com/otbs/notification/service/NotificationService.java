package com.otbs.notification.service;

import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.dto.NotificationResponseDTO;
import com.otbs.notification.dto.WebSocketPayloadDTO;
import com.otbs.notification.model.Notification;
import com.otbs.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public NotificationResponseDTO createNotification(NotificationRequestDTO requestDTO) {
        Notification notification = Notification.builder()
                .title(requestDTO.getTitle())
                .message(requestDTO.getMessage())
                .sender(requestDTO.getSender())
                .recipient(requestDTO.getRecipient())
                .type(requestDTO.getType())
                .sourceId(requestDTO.getSourceId())
                .actionUrl(requestDTO.getActionUrl())
                .createdAt(LocalDateTime.now())
                .read(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationResponseDTO responseDTO = mapToResponseDTO(savedNotification);
        log.info("Notification created: {}", responseDTO);
        sendWebSocketNotification(responseDTO);
        log.info("WebSocket notification sent: {}", responseDTO);

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(String username) {
        // Get all user-specific notifications and broadcast notifications
        List<Notification> userNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(username);
        List<Notification> broadcastNotifications = notificationRepository.findByRecipientIsNullOrderByCreatedAtDesc();

        // Combine and convert to DTOs
        return userNotifications.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotifications(String username) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(username, false);
        return unreadNotifications.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        return notificationRepository.countByRecipientAndRead(username, false);
    }

    @Transactional
    public NotificationResponseDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return mapToResponseDTO(updatedNotification);
    }

    @Transactional
    public void markAllAsRead(String username) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientAndReadOrderByCreatedAtDesc(username, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    // Helper method to convert entity to DTO
    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .sender(notification.getSender())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .sourceId(notification.getSourceId())
                .actionUrl(notification.getActionUrl())
                .build();
    }

    // Send WebSocket notification
    private void sendWebSocketNotification(NotificationResponseDTO notification) {
        WebSocketPayloadDTO payload = WebSocketPayloadDTO.builder()
                .type("NOTIFICATION")
                .data(notification)
                .build();

        if (notification.getSender() != null) {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    notification.getSender(),
                    "/queue/notifications",
                    payload
            );
            log.info("Sent notification to user: {}", notification.getSender());
        } else {
            // Broadcast notification
            messagingTemplate.convertAndSend("/topic/notifications", payload);
            log.info("Broadcasted notification to all users");
        }
    }
}
