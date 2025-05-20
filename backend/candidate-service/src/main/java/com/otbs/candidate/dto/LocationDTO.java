package com.otbs.candidate.dto;

import lombok.Builder;

@Builder
public record LocationDTO(
        Long id,
        String city,
        String country,
        String state
) {}
