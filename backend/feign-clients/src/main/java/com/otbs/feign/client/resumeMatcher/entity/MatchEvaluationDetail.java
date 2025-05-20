package com.otbs.feign.client.resumeMatcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MatchEvaluationDetail {
    @JsonProperty("raw_score")
    private int rawScore;
    @JsonProperty("weighted_score")
    private double weightedScore;
    @JsonProperty("matching_skills")
    private List<String> matchingSkills;
    @JsonProperty("missing_skills")
    private List<String> missingSkills;
    private String analysis;
}
