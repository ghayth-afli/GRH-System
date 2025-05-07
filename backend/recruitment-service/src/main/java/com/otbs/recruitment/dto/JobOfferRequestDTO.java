package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EJobOfferStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobOfferRequestDTO(

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Department is required")
        String department,

        @NotBlank(message = "Role is required")
        String role,

        @NotNull(message = "Job offer status is required")
        EJobOfferStatus status,

        @NotNull(message = "Internal/external flag is required")
        Boolean isInternal

) {}
