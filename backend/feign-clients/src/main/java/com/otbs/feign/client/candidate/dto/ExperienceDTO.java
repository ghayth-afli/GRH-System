package com.otbs.feign.client.candidate.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ExperienceDTO(
        Long id,
        List<String> achievements,
        String company,
        String endDate,
        String location,
        List<String> responsibilities,
        String startDate,
        String title
) {}