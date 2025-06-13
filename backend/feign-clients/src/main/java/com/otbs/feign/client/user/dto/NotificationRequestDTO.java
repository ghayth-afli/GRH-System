package com.otbs.feign.client.user.dto;

import com.otbs.feign.client.user.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String title;
    private String message;
    private String sender;
    private String recipient;  // null for broadcast
    private NotificationType type;
    private String sourceId;   // ID of the source item
    private String actionUrl;  // URL for action (if applicable)
}
