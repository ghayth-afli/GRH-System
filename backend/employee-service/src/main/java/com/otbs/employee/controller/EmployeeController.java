package com.otbs.employee.controller;

import com.otbs.employee.dto.EmployeeInfoRequestDTO;
import com.otbs.employee.dto.MessageResponseDTO;
import com.otbs.employee.dto.ProfilePictureDTO;
import com.otbs.employee.model.Employee;
import com.otbs.employee.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
@Tag(name = "Employee Management", description = "APIs for managing employee information and profiles")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(
            summary = "Get employee by email",
            description = "Retrieves an employee's details using their email address."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee found",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping
    public Employee getEmployeeByEmail(
            @Parameter(description = "Employee's email address", example = "john.doe@company.com", required = true)
            @RequestParam("email") String email
    ) {
        return employeeService.getEmployeeByEmail(email);
    }

    @Operation(
            summary = "Get employee by LDAP DN",
            description = "Retrieves an employee's details using their LDAP Distinguished Name (DN)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee found",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping("/{id}")
    public Employee getEmployeeByDn(
            @Parameter(description = "LDAP Distinguished Name (DN) of the employee", example = "uid=john.doe,ou=users,dc=example,dc=com")
            @PathVariable("id") String id
    ) {
        return employeeService.getEmployeeByDn(LdapUtils.newLdapName(id));
    }

    @Operation(
            summary = "Get all employees",
            description = "Retrieves a list of all employees in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of employees",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @Operation(
            summary = "Get employee by username",
            description = "Retrieves an employee's details using their username."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee found",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping("/username")
    public Employee getEmployeeByUsername(
            @Parameter(description = "Employee's username", example = "john.doe", required = true)
            @RequestParam("username") String username
    ) {
        return employeeService.getEmployeeByUsername(username);
    }

    @Operation(
            summary = "Get manager by department",
            description = "Retrieves the manager of a specific department."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manager found",
            content = @Content(schema = @Schema(implementation = Employee.class))
    )
    @ApiResponse(responseCode = "404", description = "Manager not found")
    @GetMapping("/manager")
    public Employee getManagerByDepartment(
            @Parameter(description = "Department name", example = "Engineering", required = true)
            @RequestParam("department") String department
    ) {
        return employeeService.getManagerByDepartment(department);
    }

    @Operation(
            summary = "Update employee information",
            description = "Updates an employee's details, including optional profile picture. Requires Manager, HR, or Employee role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('Employee')")
    public ResponseEntity<MessageResponseDTO> updateEmployeeInfo(
            @Parameter(description = "Employee's first name", example = "John", required = true)
            @RequestParam("firstName") String firstName,
            @Parameter(description = "Employee's last name", example = "Doe", required = true)
            @RequestParam("lastName") String lastName,
            @Parameter(description = "Employee's email address", example = "john.doe@company.com", required = true)
            @RequestParam("email") String email,
            @Parameter(description = "Profile picture file (optional)", required = false)
            @RequestParam(value = "picture", required = false) MultipartFile picture,
            @Parameter(description = "Employee's job title", example = "Software Engineer", required = true)
            @RequestParam("jobTitle") String department,
            @Parameter(description = "Employee's phone number 1", example = "+21612345678", required = true)
            @RequestParam("phoneNumber1") String phoneNumber1,
            @Parameter(description = "Employee's phone number 2", example = "+21687654321", required = false)
            @RequestParam(value = "phoneNumber2", required = false) String phoneNumber2
    ) {
        EmployeeInfoRequestDTO employeeInfoRequestDTO = new EmployeeInfoRequestDTO(firstName, lastName, email, department, phoneNumber1, phoneNumber2);
        employeeService.updateEmployeeInfo(employeeInfoRequestDTO, picture);
        return ResponseEntity.ok(new MessageResponseDTO("Employee updated successfully"));
    }

    @Operation(
            summary = "Get employee profile picture",
            description = "Retrieves an employee's profile picture by username."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Profile picture retrieved",
            content = @Content(schema = @Schema(implementation = ProfilePictureDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Profile picture not found")
    @GetMapping("/profilePicture")
    public ResponseEntity<ProfilePictureDTO> getProfilePicture(
            @Parameter(description = "Employee's username", example = "john.doe", required = true)
            @RequestParam("username") String username
    ) {
        return ResponseEntity.ok(employeeService.getProfilePicture(username));
    }
}