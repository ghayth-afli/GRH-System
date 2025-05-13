package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EApplicationStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ApplicationResponseDTO(
        Long id,
        Long candidateId,
        String FullName,
        boolean isInternal,
        String Email,
        String Phone,
        EApplicationStatus status,
        double score,
        LocalDateTime submissionDate
) {

}
