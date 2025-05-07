package com.otbs.notification.controller;


import com.otbs.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

//@RestController("/api/v1/notifications")
@RequiredArgsConstructor
public class SseController {

//    private final SseService sseService;
//
//    @GetMapping("/sse-endpoint")
//    public SseEmitter handleSse() {
//        return sseService.createEmitter();
//    }
}
