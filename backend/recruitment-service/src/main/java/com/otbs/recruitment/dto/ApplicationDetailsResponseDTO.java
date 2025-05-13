package com.otbs.recruitment.dto;

import com.otbs.feign.client.candidate.dto.CandidateResponseDTO;
import com.otbs.recruitment.model.MatchResult;
import lombok.Builder;

@Builder
public record ApplicationDetailsResponseDTO(
        Long applicationId,
        CandidateResponseDTO resume,
        MatchResult matchResult
) {
}
