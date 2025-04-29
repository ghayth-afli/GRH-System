package com.otbs.employee.controller;

import com.otbs.employee.dto.EmployeeInfoRequestDTO;
import com.otbs.employee.dto.MessageResponseDTO;
import com.otbs.employee.dto.ProfilePictureDTO;
import com.otbs.employee.model.Employee;
import com.otbs.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public Employee getEmployeeByEmail(@RequestParam("email") String email) {
        return employeeService.getEmployeeByEmail(email);
    }

    @GetMapping("/{id}")
    public Employee getEmployeeByDn(@PathVariable("id") String id) {
        return employeeService.getEmployeeByDn(LdapUtils.newLdapName(id));
    }

    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/username")
    public Employee getEmployeeByUsername(@RequestParam("username") String username) {
        return employeeService.getEmployeeByUsername(username);
    }

    //get manager by department
    @GetMapping("/manager")
    public Employee getManagerByDepartment(@RequestParam("department") String department) {
        return employeeService.getManagerByDepartment(department);
    }


    @PutMapping("/update")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('Employee')")
    public ResponseEntity<MessageResponseDTO> updateEmployeeInfo(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "picture", required = false) MultipartFile picture,
            @RequestParam("jobTitle") String department
    ) {
        EmployeeInfoRequestDTO employeeInfoRequestDTO = new EmployeeInfoRequestDTO(firstName, lastName, email, department);
        employeeService.updateEmployeeInfo(employeeInfoRequestDTO, picture);

        return ResponseEntity.ok(new MessageResponseDTO("Employee updated successfully"));
    }

    @GetMapping("/profilePicture")
    public ResponseEntity<ProfilePictureDTO> getProfilePicture(@RequestParam("username") String username) {
        return ResponseEntity.ok(employeeService.getProfilePicture(username));
    }
}