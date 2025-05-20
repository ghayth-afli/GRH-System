package com.otbs.feign.client.resumeMatcher.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record JobOfferRequestDTO(
        String title,
        String description,
        String department,
        String responsibilities,
        String qualifications,
        String role,
        Boolean isInternal
) {}
