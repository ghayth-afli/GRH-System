package com.otbs.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for resetting a user's password")
public record ResetPasswordRequestDTO(
        @Schema(description = "New password for the user",
                example = "newPassword123!",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters long")
        String password
) {}