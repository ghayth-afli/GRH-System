package com.otbs.feign.client.resumeMatcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MatchScoreDetails {
    @JsonProperty("skills_match")
    private MatchEvaluationDetail skillsMatch;
    @JsonProperty("relevant_experience")
    private MatchEvaluationDetail relevantExperience;
    private MatchEvaluationDetail education;
    private MatchEvaluationDetail certifications;
    @JsonProperty("cultural_fit")
    private MatchEvaluationDetail culturalFit;
    @JsonProperty("language_proficiency")
    private MatchEvaluationDetail languageProficiency;
    @JsonProperty("achievements_projects")
    private MatchEvaluationDetail achievementsProjects;
}
