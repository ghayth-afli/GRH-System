package com.otbs.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(
        description = "DTO for requesting a training session",
        requiredProperties = {"title", "description", "startDate", "endDate"}
)
public record TrainingRequestDTO(

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
        @Schema(description = "Title of the training session", example = "Effective Communication Skills")
        String title,

        @NotBlank(message = "Description is required")
        @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
        @Schema(description = "Description of the training session", example = "This training covers techniques for improving workplace communication.")
        String description,

        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Start date cannot be in the past")
        @Schema(description = "Start date of the training session", example = "2025-06-01")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        @FutureOrPresent(message = "End date cannot be in the past")
        @Schema(description = "End date of the training session (must be the same or after start date)", example = "2025-06-03")
        LocalDate endDate
) {
    public TrainingRequestDTO {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be the same or after the start date");
        }
    }
}