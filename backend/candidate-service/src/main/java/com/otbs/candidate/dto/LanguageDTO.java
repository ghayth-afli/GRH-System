package com.otbs.candidate.dto;

import com.otbs.candidate.model.EProficiency;

public record LanguageDTO(
        Long id,
        String language,
        EProficiency proficiency
) {}
