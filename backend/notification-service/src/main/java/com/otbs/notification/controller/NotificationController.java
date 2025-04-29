package com.otbs.notification.controller;

import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.dto.NotificationResponseDTO;
import com.otbs.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public void createNotification(@RequestBody NotificationRequestDTO requestDTO) {
        notificationService.createNotification(requestDTO);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications() {
        String id = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUserNotifications(id);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications() {
        String id = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(id);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        String id = getCurrentUserId();
        long count = notificationService.getUnreadCount(id);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        NotificationResponseDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        String id = getCurrentUserId();
        notificationService.markAllAsRead(id);
        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
