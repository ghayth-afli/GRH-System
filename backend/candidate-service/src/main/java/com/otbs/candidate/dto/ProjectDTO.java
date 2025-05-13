package com.otbs.candidate.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ProjectDTO(
        Long id,
        String name,
        String description,
        List<String> technologies,
        String url
) {}
