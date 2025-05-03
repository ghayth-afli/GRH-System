package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EApplicantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequestDTO(

        @NotNull(message = "Job offer ID is required")
        Long jobOfferId,

        @NotBlank(message = "Applicant identifier is required")
        String applicantIdentifier,

        @NotNull(message = "Applicant type is required")
        EApplicantType applicantType,

        byte[] resume,
        byte[] coverLetter

) {}
