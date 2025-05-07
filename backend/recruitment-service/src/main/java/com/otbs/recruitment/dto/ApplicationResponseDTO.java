package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EApplicantType;
import com.otbs.recruitment.model.EApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDTO(
        Long id,
        Long jobOfferId,
        String applicantIdentifier,
        EApplicantType applicantType,
        EApplicationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
