package com.otbs.feign.client.resumeMatcher.dto;

import com.otbs.feign.client.resumeMatcher.entity.*;
import lombok.Data;
import java.util.List;

@Data
public class ParsedResumeResponse {
    private CandidateInfo candidateInfo;
    private Skills skills;
    private List<Experience> experience;
    private List<Education> education;
    private List<Certification> certifications;
    private List<Language> languages;
    private List<Project> projects;
}
