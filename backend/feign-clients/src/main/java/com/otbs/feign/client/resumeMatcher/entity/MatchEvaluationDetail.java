package com.otbs.feign.client.resumeMatcher.entity;

import lombok.Data;
import java.util.List;

@Data
public class MatchEvaluationDetail {
    private int rawScore;
    private double weightedScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String analysis;
}
