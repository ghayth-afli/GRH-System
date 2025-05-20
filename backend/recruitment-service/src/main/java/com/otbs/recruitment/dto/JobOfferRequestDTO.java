package com.otbs.recruitment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobOfferRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotBlank(message = "Department is required")
        @Size(max = 100, message = "Department name must not exceed 100 characters")
        String department,

        @NotBlank(message = "Responsibilities are required")
        @Size(max = 2000, message = "Responsibilities must not exceed 2000 characters")
        String responsibilities,

        @NotBlank(message = "Qualifications are required")
        @Size(max = 2000, message = "Qualifications must not exceed 2000 characters")
        String qualifications,

        @NotBlank(message = "Role is required")
        @Size(max = 100, message = "Role name must not exceed 100 characters")
        String role,

        @NotNull(message = "Internal status is required")
        Boolean isInternal
) {}
