package com.otbs.recruitment.dto;

import com.otbs.feign.client.resumeMatcher.dto.ParsedResumeResponse;
import com.otbs.recruitment.model.EApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponseDTO(
        Long id,
        ParsedResumeResponse resume,
        EApplicationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
