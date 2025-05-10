package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EJobOfferStatus;

import java.time.LocalDateTime;

public record JobOfferResponseDTO(
        Long id,
        String title,
        String description,
        String department,
        String responsibilities,
        String qualifications,
        String role,
        EJobOfferStatus status,
        Boolean isInternal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
