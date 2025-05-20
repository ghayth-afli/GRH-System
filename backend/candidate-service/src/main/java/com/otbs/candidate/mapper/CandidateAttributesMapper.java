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
        return CandidateResponseDTO.builder()
                .id(entity.getId())
                .candidateInfo(toCandidateInfoDTO(entity.getCandidateInfo()))
                .certifications(toCertificationDTOList(entity.getCertifications()))
                .education(toEducationDTOList(entity.getEducation()))
                .experience(toExperienceDTOList(entity.getExperience()))
                .languages(toLanguageDTOList(entity.getLanguages()))
                .projects(toProjectDTOList(entity.getProjects()))
                .skills(toSkillsDTO(entity.getSkills()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CandidateInfoDTO toCandidateInfoDTO(CandidateInfo candidateInfo) {
        if (candidateInfo == null) {
            return null;
        }

        return CandidateInfoDTO.builder()
                .id(candidateInfo.getId())
                .email(candidateInfo.getEmail())
                .linkedin(candidateInfo.getLinkedin())
                .location(toLocationDTO(candidateInfo.getLocation()))
                .name(candidateInfo.getName())
                .phone(candidateInfo.getPhone())
                .website(candidateInfo.getWebsite())
                .build();
    }

    public LocationDTO toLocationDTO(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDTO.builder()
                .id(location.getId())
                .city(location.getCity())
                .country(location.getCountry())
                .state(location.getState())
                .build();
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
        return CertificationDTO.builder()
                .id(certification.getId())
                .name(certification.getName())
                .build();
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
        return EducationDTO.builder()
                .id(education.getId())
                .degree(education.getDegree())
                .endDate(education.getEndDate())
                .fieldOfStudy(education.getFieldOfStudy())
                .institution(education.getInstitution())
                .location(education.getLocation())
                .startDate(education.getStartDate())
                .build();
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
        return ExperienceDTO.builder()
                .id(experience.getId())
                .achievements(experience.getAchievements())
                .company(experience.getCompany())
                .endDate(experience.getEndDate())
                .location(experience.getLocation())
                .responsibilities(experience.getResponsibilities())
                .startDate(experience.getStartDate())
                .title(experience.getTitle())
                .build();
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
        return LanguageDTO.builder()
                .id(language.getId())
                .language(language.getLanguage())
                .proficiency(language.getProficiency())
                .build();
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
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .technologies(project.getTechnologies())
                .url(project.getUrl())
                .build();
    }

    public SkillsDTO toSkillsDTO(Skills skills) {
        if (skills == null) {
            return null;
        }

        return SkillsDTO.builder()
                .id(skills.getId())
                .soft(skills.getSoft())
                .technical(skills.getTechnical())
                .build();
    }
}