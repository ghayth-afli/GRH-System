package com.otbs.candidate.dto;

import lombok.Builder;

@Builder
public record CertificationDTO(
        Long id,
        String name
) {}
