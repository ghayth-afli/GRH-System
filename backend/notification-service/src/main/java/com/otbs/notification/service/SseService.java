package com.otbs.notification.service;

import com.otbs.common.event.Event;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter createEmitter();
    void broadcast(Event event);
}
