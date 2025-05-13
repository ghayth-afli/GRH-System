package com.otbs.candidate.dto;

import lombok.Builder;

@Builder
public record EducationDTO(
        Long id,
        String degree,
        String endDate,
        String fieldOfStudy,
        String institution,
        String location,
        String startDate
) {}
