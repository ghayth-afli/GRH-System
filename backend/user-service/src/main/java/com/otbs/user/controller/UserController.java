package com.otbs.user.controller;

import com.otbs.user.dto.UserInfoRequestDTO;
import com.otbs.user.dto.MessageResponseDTO;
import com.otbs.user.dto.ProfilePictureDTO;
import com.otbs.user.model.User;
import com.otbs.user.service.UserService;
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

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing user information and profiles")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Get user by email",
            description = "Retrieves an user's details using their email address."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping
    public User getUserByEmail(
            @Parameter(description = "User's email address", example = "john.doe@company.com", required = true)
            @RequestParam("email") String email
    ) {
        return userService.getUserByEmail(email);
    }

    @Operation(
            summary = "Get user by LDAP DN",
            description = "Retrieves an user's details using their LDAP Distinguished Name (DN)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public User getUserByDn(
            @Parameter(description = "LDAP Distinguished Name (DN) of the user", example = "uid=john.doe,ou=users,dc=example,dc=com")
            @PathVariable("id") String id
    ) {
        return userService.getUserByDn(LdapUtils.newLdapName(id));
    }

    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all users in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(
            summary = "Get user by username",
            description = "Retrieves an user's details using their username."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User found",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/username")
    public User getUserByUsername(
            @Parameter(description = "User's username", example = "john.doe", required = true)
            @RequestParam("username") String username
    ) {
        log.info("Retrievingggggggg user: {}", userService.getUserByUsername(username));
        return userService.getUserByUsername(username);
    }

    @Operation(
            summary = "Get manager by department",
            description = "Retrieves the manager of a specific department."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Manager found",
            content = @Content(schema = @Schema(implementation = User.class))
    )
    @ApiResponse(responseCode = "404", description = "Manager not found")
    @GetMapping("/manager")
    public User getManagerByDepartment(
            @Parameter(description = "Department name", example = "Engineering", required = true)
            @RequestParam("department") String department
    ) {
        return userService.getManagerByDepartment(department);
    }

    @Operation(
            summary = "Update user information",
            description = "Updates an user's details, including optional profile picture. Requires Manager, HR, or User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PatchMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('HRD')")
    public ResponseEntity<MessageResponseDTO> updateUserInfo(
            @Parameter(description = "User's first name", example = "John", required = true)
            @RequestParam("firstName") String firstName,
            @Parameter(description = "User's last name", example = "Doe", required = true)
            @RequestParam("lastName") String lastName,
            @Parameter(description = "User's email address", example = "john.doe@company.com", required = true)
            @RequestParam("email") String email,
            @Parameter(description = "Profile picture file (optional)", required = false)
            @RequestParam(value = "picture", required = false) MultipartFile picture,
            @Parameter(description = "User's job title", example = "Software Engineer", required = true)
            @RequestParam("jobTitle") String department,
            @Parameter(description = "User's phone number 1", example = "+21612345678", required = true)
            @RequestParam("phoneNumber1") String phoneNumber1,
            @Parameter(description = "User's phone number 2", example = "+21687654321", required = false)
            @RequestParam(value = "phoneNumber2", required = false) String phoneNumber2,
            @Parameter(description = "User's gender", example = "Male", required = false)
            @RequestParam(value = "gender", required = false) String gender,
            @Parameter(description = "User's birthdate in YYYY-MM-DD format", example = "1990-01-01", required = false)
            @RequestParam(value = "birthDate", required = false) String birthdate
            ) {
        UserInfoRequestDTO userInfoRequestDTO = new UserInfoRequestDTO(firstName, lastName, email, department, phoneNumber1, phoneNumber2,gender,birthdate);
        userService.updateUserInfo(userInfoRequestDTO, picture);
        return ResponseEntity.ok(new MessageResponseDTO("User updated successfully"));
    }

    @Operation(
            summary = "Get user profile picture",
            description = "Retrieves an user's profile picture by username."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Profile picture retrieved",
            content = @Content(schema = @Schema(implementation = ProfilePictureDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Profile picture not found")
    @GetMapping("/profilePicture")
    public ResponseEntity<ProfilePictureDTO> getProfilePicture(
            @Parameter(description = "User's username", example = "john.doe", required = true)
            @RequestParam("username") String username
    ) {
        return ResponseEntity.ok(userService.getProfilePicture(username));
    }
}