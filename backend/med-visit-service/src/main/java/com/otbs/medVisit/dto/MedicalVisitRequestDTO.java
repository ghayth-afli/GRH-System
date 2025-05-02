package com.otbs.medVisit.dto;

import com.otbs.medVisit.exception.InvalidMedicalVisitRequestException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(
        description = "DTO for requesting a medical visit",
        requiredProperties = {"doctorName", "visitDate", "startTime", "endTime"}
)
public record MedicalVisitRequestDTO(
        @NotBlank(message = "Doctor name is required")
        @Size(min = 2, max = 100, message = "Doctor name must be between 2 and 100 characters")
        @Schema(description = "Name of the doctor for the visit", example = "Dr. Jane Smith")
        String doctorName,

        @NotNull(message = "Visit date is required")
        @FutureOrPresent(message = "Visit date must be today or in the future")
        @Schema(description = "Date of the medical visit", example = "2025-06-01")
        LocalDate visitDate,

        @NotNull(message = "Start time is required")
        @Schema(description = "Start time of the medical visit", example = "10:00:00")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        @Schema(description = "End time of the medical visit (must be after start time)", example = "10:30:00")
        LocalTime endTime
) {
    public MedicalVisitRequestDTO {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new InvalidMedicalVisitRequestException("End time must be after start time");
        }
    }
}