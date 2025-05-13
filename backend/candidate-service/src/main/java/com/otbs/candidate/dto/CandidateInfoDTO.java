package com.otbs.candidate.dto;

import lombok.Builder;

@Builder
public record CandidateInfoDTO(
        Long id,
        String email,
        String linkedin,
        LocationDTO location,
        String name,
        String phone,
        String website
) {}
