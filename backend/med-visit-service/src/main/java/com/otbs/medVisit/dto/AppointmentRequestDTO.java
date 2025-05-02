package com.otbs.medVisit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Schema(
        description = "DTO for requesting a medical appointment",
        requiredProperties = {"medicalVisitId", "timeSlot"}
)
public record AppointmentRequestDTO(
        @NotNull(message = "Medical visit ID is required")
        @Positive(message = "Medical visit ID must be a positive number")
        @Schema(description = "Unique identifier for the medical visit", example = "1")
        Long medicalVisitId,

        @NotNull(message = "Time slot is required")
        @Future(message = "Time slot must be in the future")
        @Schema(description = "Requested time slot for the appointment", example = "2025-06-01T10:00:00")
        LocalDateTime timeSlot
) {}