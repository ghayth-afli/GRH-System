package com.otbs.feign.client.user;

import com.otbs.feign.client.user.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", url = "http://localhost:8082",contextId ="userClient")
public interface UserClient {

    @GetMapping("/api/v1/users")
    UserResponse getUserByEmail(@RequestParam("email") String email);

    @GetMapping("/api/v1/users/{id}")
    UserResponse getUserByDn(@PathVariable("id") String id);

    @GetMapping("/api/v1/users/all")
    List<UserResponse> getAllUsers();

    @GetMapping("/api/v1/users/username")
    UserResponse getUserByUsername(@RequestParam("username") String username);

    @GetMapping("/api/v1/users/manager")
    UserResponse getManagerByDepartment(@RequestParam("department") String department);
}