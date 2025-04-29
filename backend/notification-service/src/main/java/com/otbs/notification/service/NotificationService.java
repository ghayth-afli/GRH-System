package com.otbs.notification.service;

import com.otbs.notification.dto.MailRequestDTO;
import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.dto.NotificationResponseDTO;
import com.otbs.notification.dto.WebSocketPayloadDTO;
import com.otbs.notification.exception.NotificationException;
import com.otbs.notification.model.Notification;
import com.otbs.notification.repository.NotificationRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

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
        NotificationResponseDTO responseDTO = mapToResponseDTO(savedNotification);
        sendWebSocketNotification(responseDTO);

        return responseDTO;
    }

    public void sendMail(MailRequestDTO mailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            log.info("Sending email to: {}", mailRequest.getTo());
            helper.setTo(mailRequest.getTo());
            helper.setSubject(mailRequest.getSubject());

            Context context = new Context();
            context.setVariable("subject", mailRequest.getSubject());
            context.setVariable("text", mailRequest.getBody());

            String htmlContent = templateEngine.process("email-template", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);
        }
        catch (Exception e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUserNotifications(String username) {
        List<Notification> userNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(username);
        List<Notification> broadcastNotifications = notificationRepository.findByRecipientIsNullOrderByCreatedAtDesc();

        List<Notification> allNotifications = Stream.concat(userNotifications.stream(), broadcastNotifications.stream())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .toList();

        return allNotifications.stream()
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
                .orElseThrow(() -> new NotificationException("Notification not found with id: " + id));

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

    private NotificationResponseDTO mapToResponseDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .sender(notification.getSender())
                .recipient(notification.getRecipient())
                .type(notification.getType())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .sourceId(notification.getSourceId())
                .actionUrl(notification.getActionUrl())
                .build();
    }

    private void sendWebSocketNotification(NotificationResponseDTO notification) {
        WebSocketPayloadDTO payload = WebSocketPayloadDTO.builder()
                .type("NOTIFICATION")
                .data(notification)
                .build();

        log.info("Sending WebSocket notification: {}", notification);
        messagingTemplate.convertAndSend("/topic/notifications", payload);
    }
}