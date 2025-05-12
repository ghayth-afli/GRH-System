package com.otbs.candidate.dto;

import java.util.List;

public record SkillsDTO(
        Long id,
        List<String> soft,
        List<String> technical
) {}
