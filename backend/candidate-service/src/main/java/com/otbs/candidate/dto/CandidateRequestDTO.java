package com.otbs.candidate.dto;

import com.otbs.candidate.model.*;

import java.util.List;

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
