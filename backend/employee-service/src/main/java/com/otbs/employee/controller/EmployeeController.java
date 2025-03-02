package com.otbs.employee.controller;

import com.otbs.employee.model.Employee;
import com.otbs.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/v1/employee")
@RequiredArgsConstructor
@Slf4j
@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<?> getEmployeeByEmail(@RequestParam("email") String email) {
        Employee ldapUser = employeeService.getEmployeeByEmail(email);
        return ResponseEntity.ok(ldapUser);
    }

    // Get employee by dn
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeByDn(@PathVariable("id") String id) {
        return ResponseEntity.ok(employeeService.getEmployeeByDn(LdapUtils.newLdapName(id)));
    }

    // Get all employees
    @GetMapping("/all")
    public ResponseEntity<?> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/username")
    public ResponseEntity<?> getEmployeeByUsername(@RequestParam("username") String username) {
        return ResponseEntity.ok(employeeService.getEmployeeByUsername(username));
    }
}
