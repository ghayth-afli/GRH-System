package com.otbs.feign.client;

import com.otbs.feign.dto.EmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "employee-service", url = "http://localhost:8082")
public interface EmployeeClient {

    @GetMapping("api/v1/employee")
    ResponseEntity<EmployeeResponse> getEmployeeByEmail(@RequestParam("email") String email);

    @GetMapping("api/v1/employee/{id}")
    ResponseEntity<EmployeeResponse> getEmployeeByDn(@PathVariable("id") String id);

    @GetMapping("api/v1/employee/all")
    ResponseEntity<List<EmployeeResponse>> getAllEmployees();

    @GetMapping("api/v1/employee/username")
    ResponseEntity<EmployeeResponse> getEmployeeByUsername(@RequestParam("username") String username);

    @GetMapping("api/v1/employee/manager")
    ResponseEntity<EmployeeResponse> getManagerByDepartment(@RequestParam("department") String department);
}