package com.otbs.notification.service;

import com.otbs.common.event.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseServiceImpl implements SseService {
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("Emitter completed");
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.warn("Emitter timed out");
        });
        log.info("New SSE emitter created. Total emitters: {}", emitters.size());
        return emitter;
    }

    @Override
    public void broadcast(Event event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(event.getEventType()).data(event));
                log.info("Sent event to emitter: {}", event);
            } catch (IOException e) {
                log.error("Failed to send event to emitter: {}", e.getMessage());
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
