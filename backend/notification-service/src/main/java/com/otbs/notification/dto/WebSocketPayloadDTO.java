package com.otbs.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketPayloadDTO {
    private String type;  // "NOTIFICATION", "LEAVE_REQUEST_UPDATE", etc.
    private Object data;  // The actual payload (NotificationResponseDTO, etc.)
}
