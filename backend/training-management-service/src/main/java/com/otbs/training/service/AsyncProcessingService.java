package com.otbs.training.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncProcessingService {
    private final NotificationService notificationService;

    @Async("taskExecutor")
    public void sendAppNotification(String to, String subject, String body, Long sourceId, String actionUrl) {
        try {
            notificationService.sendAppNotification(to, subject, body, sourceId, actionUrl);
        } catch (Exception e) {
            log.error("Error sending app notification: {}", e.getMessage(), e);
        }
    }

    @Async("taskExecutor")
    public void sendMailNotification(String to, String subject, String body) {
        try {
            notificationService.sendMailNotification(to, subject, body);
        } catch (Exception e) {
            log.error("Error sending mail notification: {}", e.getMessage(), e);
        }
    }
}
