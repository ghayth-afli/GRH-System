package com.otbs.auth.controller;

import com.otbs.auth.dto.JwtResponseDTO;
import com.otbs.auth.dto.AuthRequestDTO;
import com.otbs.auth.dto.RefreshRequestDTO;
import com.otbs.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for user authentication and token management")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@RestController
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate User", description = "Authenticates a user with username and password and returns JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated, returns access and refresh tokens",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@RequestBody AuthRequestDTO authRequestDTO) {
        JwtResponseDTO response = authService.authenticateUser(authRequestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh Token", description = "Refreshes JWT access token using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully refreshed, returns new access and refresh tokens",
                    content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponseDTO> refreshToken(@RequestBody RefreshRequestDTO refreshRequestDTO) {
        JwtResponseDTO response = authService.refreshToken(refreshRequestDTO);
        return ResponseEntity.ok(response);
    }
}