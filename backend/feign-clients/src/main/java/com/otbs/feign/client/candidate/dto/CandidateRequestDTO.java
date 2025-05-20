package com.otbs.feign.client.candidate.dto;

import com.otbs.feign.client.resumeMatcher.entity.*;
import lombok.Builder;

import java.util.List;

@Builder
public record CandidateRequestDTO(
        CandidateInfo candidateInfo,
        List<Certification> certifications,
        List<Education> education,
        List<Experience> experience,
        List<Language> languages,
        List<Project> projects,
        Skills skills
) {
}
