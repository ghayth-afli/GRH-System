package com.otbs.candidate.mapper;

import com.otbs.candidate.dto.*;
import com.otbs.candidate.model.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                toCandidateInfoDTO(entity.getCandidateInfo()),
                toCertificationDTOList(entity.getCertifications()),
                toEducationDTOList(entity.getEducation()),
                toExperienceDTOList(entity.getExperience()),
                toLanguageDTOList(entity.getLanguages()),
                toProjectDTOList(entity.getProjects()),
                toSkillsDTO(entity.getSkills()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public CandidateInfoDTO toCandidateInfoDTO(CandidateInfo candidateInfo) {
        if (candidateInfo == null) {
            return null;
        }

        return new CandidateInfoDTO(
                candidateInfo.getId(),
                candidateInfo.getEmail(),
                candidateInfo.getLinkedin(),
                toLocationDTO(candidateInfo.getLocation()),
                candidateInfo.getName(),
                candidateInfo.getPhone(),
                candidateInfo.getWebsite()
        );
    }

    public LocationDTO toLocationDTO(Location location) {
        if (location == null) {
            return null;
        }

        return new LocationDTO(
                location.getId(),
                location.getCity(),
                location.getCountry(),
                location.getState()
        );
    }

    public List<CertificationDTO> toCertificationDTOList(List<Certification> certifications) {
        if (certifications == null) {
            return null;
        }

        return certifications.stream()
                .map(this::toCertificationDTO)
                .collect(Collectors.toList());
    }

    public CertificationDTO toCertificationDTO(Certification certification) {
        return new CertificationDTO(
                certification.getId(),
                certification.getName()
        );
    }

    public List<EducationDTO> toEducationDTOList(List<Education> educations) {
        if (educations == null) {
            return null;
        }

        return educations.stream()
                .map(this::toEducationDTO)
                .collect(Collectors.toList());
    }

    public EducationDTO toEducationDTO(Education education) {
        return new EducationDTO(
                education.getId(),
                education.getDegree(),
                education.getEndDate(),
                education.getFieldOfStudy(),
                education.getInstitution(),
                education.getLocation(),
                education.getStartDate()
        );
    }

    public List<ExperienceDTO> toExperienceDTOList(List<Experience> experiences) {
        if (experiences == null) {
            return null;
        }

        return experiences.stream()
                .map(this::toExperienceDTO)
                .collect(Collectors.toList());
    }

    public ExperienceDTO toExperienceDTO(Experience experience) {
        return new ExperienceDTO(
                experience.getId(),
                experience.getAchievements(),
                experience.getCompany(),
                experience.getEndDate(),
                experience.getLocation(),
                experience.getResponsibilities(),
                experience.getStartDate(),
                experience.getTitle()
        );
    }

    public List<LanguageDTO> toLanguageDTOList(List<Language> languages) {
        if (languages == null) {
            return null;
        }

        return languages.stream()
                .map(this::toLanguageDTO)
                .collect(Collectors.toList());
    }

    public LanguageDTO toLanguageDTO(Language language) {
        return new LanguageDTO(
                language.getId(),
                language.getLanguage(),
                language.getProficiency()
        );
    }

    public List<ProjectDTO> toProjectDTOList(List<Project> projects) {
        if (projects == null) {
            return null;
        }

        return projects.stream()
                .map(this::toProjectDTO)
                .collect(Collectors.toList());
    }

    public ProjectDTO toProjectDTO(Project project) {
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getTechnologies(),
                project.getUrl()
        );
    }

    public SkillsDTO toSkillsDTO(Skills skills) {
        if (skills == null) {
            return null;
        }

        return new SkillsDTO(
                skills.getId(),
                skills.getSoft(),
                skills.getTechnical()
        );
    }
}