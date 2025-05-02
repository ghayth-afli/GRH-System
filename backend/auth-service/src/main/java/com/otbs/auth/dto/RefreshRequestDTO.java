package com.otbs.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for refreshing JWT tokens")
public record RefreshRequestDTO(
        @Schema(description = "Refresh token to generate new access token",
                example = "eyJhbGciOiJIUzI1NiIsIn...",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Refresh token cannot be empty")
        String refreshToken
) {}