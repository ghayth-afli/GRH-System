package com.otbs.feign.client.candidate.dto;

import lombok.Builder;

@Builder
public record CertificationDTO(
        Long id,
        String name
) {}
