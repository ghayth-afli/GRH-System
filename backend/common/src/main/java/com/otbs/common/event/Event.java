package com.otbs.common.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private String eventType;
    private String entityId;
    private String entityType;
    private Map<String, Object> payload;
    private LocalDateTime timestamp;

    // Constructor for convenience
    public Event(String eventType, String entityId, String entityType, Map<String, Object> payload) {
        this.eventType = eventType;
        this.entityId = entityId;
        this.entityType = entityType;
        this.payload = payload != null ? payload : new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    // Ensure timestamp is set if not provided
    @Builder.Default
    private LocalDateTime timestampDefault = LocalDateTime.now();
}
