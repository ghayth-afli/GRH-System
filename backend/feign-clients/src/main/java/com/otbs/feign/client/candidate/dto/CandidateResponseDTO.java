package com.otbs.feign.client.candidate.dto;

import com.otbs.feign.client.resumeMatcher.entity.*;

import java.time.LocalDateTime;
import java.util.List;

public record CandidateResponseDTO(
        Long id,
        CandidateInfo candidateInfo,
        List<Certification> certifications,
        List<Education> education,
        List<Experience> experience,
        List<Language> languages,
        List<Project> projects,
        Skills skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
