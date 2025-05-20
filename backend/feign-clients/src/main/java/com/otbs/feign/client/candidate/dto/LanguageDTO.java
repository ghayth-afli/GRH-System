package com.otbs.feign.client.candidate.dto;

import com.otbs.candidate.model.EProficiency;
import lombok.Builder;

@Builder
public record LanguageDTO(
        Long id,
        String language,
        EProficiency proficiency
) {}
