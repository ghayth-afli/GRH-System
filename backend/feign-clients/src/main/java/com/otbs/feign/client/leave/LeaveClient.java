package com.otbs.feign.client.leave;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "leave-service", url = "http://localhost:8083",contextId ="leaveClient")
public interface LeaveClient {
    @GetMapping("/api/v1/leave/exists")
    ResponseEntity<Boolean> leaveExists(
            @RequestParam("userDn") String userDn,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
