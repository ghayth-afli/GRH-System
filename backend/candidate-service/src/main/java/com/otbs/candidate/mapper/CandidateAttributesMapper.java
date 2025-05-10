package com.otbs.candidate.mapper;

import com.otbs.candidate.dto.CandidateRequestDTO;
import com.otbs.candidate.dto.CandidateResponseDTO;
import com.otbs.candidate.model.Candidate;
import com.otbs.candidate.model.Certification;
import com.otbs.candidate.model.Education;
import com.otbs.candidate.model.Experience;
import com.otbs.candidate.model.Language;
import com.otbs.candidate.model.Project;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CandidateAttributesMapper {
    public Candidate toEntity(CandidateRequestDTO dto) {
        Candidate candidate = Candidate.builder()
                .candidateInfo(dto.candidateInfo())
                .skills(dto.skills())
                .build();

        // Set candidate reference in collections
        List<Certification> certifications = Optional.ofNullable(dto.certifications())
                .orElse(Collections.emptyList());
        certifications.forEach(cert -> cert.setCandidate(candidate));

        List<Education> education = Optional.ofNullable(dto.education())
                .orElse(Collections.emptyList());
        education.forEach(edu -> edu.setCandidate(candidate));

        List<Experience> experience = Optional.ofNullable(dto.experience())
                .orElse(Collections.emptyList());
        experience.forEach(exp -> exp.setCandidate(candidate));

        List<Language> languages = Optional.ofNullable(dto.languages())
                .orElse(Collections.emptyList());
        languages.forEach(lang -> lang.setCandidate(candidate));

        List<Project> projects = Optional.ofNullable(dto.projects())
                .orElse(Collections.emptyList());
        projects.forEach(proj -> proj.setCandidate(candidate));

        candidate.setCertifications(certifications);
        candidate.setEducation(education);
        candidate.setExperience(experience);
        candidate.setLanguages(languages);
        candidate.setProjects(projects);

        return candidate;
    }

    public CandidateResponseDTO toResponseDTO(Candidate entity) {
        return new CandidateResponseDTO(
                entity.getId(),
                entity.getCandidateInfo(),
                entity.getCertifications(),
                entity.getEducation(),
                entity.getExperience(),
                entity.getLanguages(),
                entity.getProjects(),
                entity.getSkills(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
