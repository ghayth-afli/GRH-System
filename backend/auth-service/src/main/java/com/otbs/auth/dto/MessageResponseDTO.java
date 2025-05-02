package com.otbs.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a message")
public record MessageResponseDTO(
        @Schema(description = "Message describing the result of the operation", example = "Password reset email sent successfully")
        String message
) {}