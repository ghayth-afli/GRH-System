package com.otbs.candidate.dto;

import java.util.List;

public record ProjectDTO(
        Long id,
        String name,
        String description,
        List<String> technologies,
        String url
) {}
