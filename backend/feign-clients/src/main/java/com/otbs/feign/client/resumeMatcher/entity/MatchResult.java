package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;
import java.util.List;

@Data
public class MatchResult {
    private double score;
    private int rawScore;
    private String interpretation;
    private MatchScoreDetails details;
    private List<String> redFlags;
    private List<String> bonusPoints;
    private String roleType;
    private double roleConfidence;
    private List<Criterion> adaptedCriteria;
    private List<String> roleSpecificInsights;

    @Data
    public static class Criterion {
        private String name;
        private int weight;
    }
}
