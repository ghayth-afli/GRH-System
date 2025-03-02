package com.otbs.feign.client;

import com.otbs.feign.dto.EmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "employee-service",
        url = "http://localhost:8082"
)
public interface EmployeeClient {


    @GetMapping("api/v1/employee")
    public ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam("email") String email);

    @GetMapping("api/v1/employee/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeByDn(@PathVariable("id") String id);

    @GetMapping("api/v1/employee/all")
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees();

    @GetMapping("api/v1/employee/username")
    public ResponseEntity<EmployeeResponse> getEmployeeByUsername(@RequestParam("username") String username);
}
