package com.otbs.notification.controller;

import com.otbs.notification.dto.NotificationRequestDTO;
import com.otbs.notification.dto.NotificationResponseDTO;
import com.otbs.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notification Management", description = "APIs for managing user notifications")
@SecurityRequirement(name = "bearerAuth")
@AllArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(
            summary = "Create a notification",
            description = "Creates a new notification for a user. Requires authentication."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Notification created successfully",
            content = @Content
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PostMapping
    public void createNotification(
            @RequestBody NotificationRequestDTO requestDTO
    ) {
        notificationService.createNotification(requestDTO);
    }

    @Operation(
            summary = "Get all user notifications",
            description = "Retrieves a list of all notifications for the authenticated user. Requires authentication."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of notifications",
            content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getUserNotifications() {
        String id = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUserNotifications(id);
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Get unread notifications",
            description = "Retrieves a list of unread notifications for the authenticated user. Requires authentication."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of unread notifications",
            content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications() {
        String id = getCurrentUserId();
        List<NotificationResponseDTO> notifications = notificationService.getUnreadNotifications(id);
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Get unread notification count",
            description = "Retrieves the count of unread notifications for the authenticated user. Requires authentication."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Count of unread notifications",
            content = @Content(schema = @Schema(type = "integer", format = "int64", example = "5"))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount() {
        String id = getCurrentUserId();
        long count = notificationService.getUnreadCount(id);
        return ResponseEntity.ok(count);
    }

    @Operation(
            summary = "Mark a notification as read",
            description = "Marks a specific notification as read by ID. Requires authentication."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Notification marked as read",
            content = @Content(schema = @Schema(implementation = NotificationResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Notification not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @Parameter(description = "ID of the notification", example = "1")
            @PathVariable Long id
    ) {
        NotificationResponseDTO notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks all notifications for the authenticated user as read. Requires authentication."
    )
    @ApiResponse(
            responseCode = "204",
            description = "All notifications marked as read",
            content = @Content
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
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