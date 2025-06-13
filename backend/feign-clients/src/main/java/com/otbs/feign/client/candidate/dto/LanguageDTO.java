package com.otbs.feign.client.candidate.dto;

import lombok.Builder;

@Builder
public record LanguageDTO(
        Long id,
        String language,
        EProficiency proficiency
) {}
