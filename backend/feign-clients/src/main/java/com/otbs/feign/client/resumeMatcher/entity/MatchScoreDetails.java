package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;

@Data
public class MatchScoreDetails {
    private MatchEvaluationDetail skillsMatch;
    private MatchEvaluationDetail relevantExperience;
    private MatchEvaluationDetail education;
    private MatchEvaluationDetail certifications;
    private MatchEvaluationDetail culturalFit;
    private MatchEvaluationDetail languageProficiency;
    private MatchEvaluationDetail achievementsProjects;
}
