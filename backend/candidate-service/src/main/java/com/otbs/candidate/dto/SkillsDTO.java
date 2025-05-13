package com.otbs.candidate.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SkillsDTO(
        Long id,
        List<String> soft,
        List<String> technical
) {}
