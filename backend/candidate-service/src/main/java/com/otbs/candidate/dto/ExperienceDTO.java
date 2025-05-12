package com.otbs.candidate.dto;

import java.util.List;

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