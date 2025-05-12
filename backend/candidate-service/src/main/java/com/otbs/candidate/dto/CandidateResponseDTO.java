package com.otbs.candidate.dto;


import java.time.LocalDateTime;
import java.util.List;

public record CandidateResponseDTO(
        Long id,
        CandidateInfoDTO candidateInfo,
        List<CertificationDTO> certifications,
        List<EducationDTO> education,
        List<ExperienceDTO> experience,
        List<LanguageDTO> languages,
        List<ProjectDTO> projects,
        SkillsDTO skills,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}