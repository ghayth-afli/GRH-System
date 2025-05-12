package com.otbs.feign.client.resumeMatcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MatchResult {
    private double score;
    @JsonProperty("raw_score")
    private int rawScore;
    private String interpretation;
    private MatchScoreDetails details;
    @JsonProperty("red_flags")
    private List<String> redFlags;
    @JsonProperty("bonus_points")
    private List<String> bonusPoints;
    @JsonProperty("role_type")
    private String roleType;
    @JsonProperty("role_confidence")
    private double roleConfidence;
    @JsonProperty("adapted_criteria")
    private List<Criterion> adaptedCriteria;
    @JsonProperty("role_specific_insights")
    private List<String> roleSpecificInsights;

    @Data
    public static class Criterion {
        private String name;
        private int weight;
    }
}
