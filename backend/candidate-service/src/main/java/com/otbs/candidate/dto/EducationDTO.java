package com.otbs.candidate.dto;

public record EducationDTO(
        Long id,
        String degree,
        String endDate,
        String fieldOfStudy,
        String institution,
        String location,
        String startDate
) {}
