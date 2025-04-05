package com.otbs.feign.client;

import com.otbs.feign.dto.NotificationRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service", url = "http://localhost:8086")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    void createNotification(@RequestBody NotificationRequestDTO requestDTO);
}
