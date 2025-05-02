package com.otbs.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for sending a password reset link")
public record ForgotPasswordRequestDTO(
        @Schema(description = "Email address of the user requesting a password reset",
                example = "john.doe@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email
) {}