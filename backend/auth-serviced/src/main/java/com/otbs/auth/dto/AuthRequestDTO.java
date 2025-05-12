package com.otbs.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for user authentication")
public record AuthRequestDTO(
        @Schema(description = "Username", example = "john20", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Username cannot be empty")
        String username,

        @Schema(description = "Password of the user", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password cannot be empty")
        String password
) {}