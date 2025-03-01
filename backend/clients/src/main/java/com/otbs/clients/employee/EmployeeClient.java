package com.otbs.clients.employee;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "employee-service",
        url = "http://localhost:8082"
)
public interface EmployeeClient {


    @GetMapping("api/v1/employee")
    public ResponseEntity<?> getEmployeeByEmail(@RequestParam("email") String email);

    @GetMapping("api/v1/employee/{id}")
    public ResponseEntity<?> getEmployeeByDn(@PathVariable("id") String id);

    @GetMapping("api/v1/employee/all")
    public ResponseEntity<?> getAllEmployees();
}
