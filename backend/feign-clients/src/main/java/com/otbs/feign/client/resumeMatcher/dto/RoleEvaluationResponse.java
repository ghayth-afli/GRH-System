package com.otbs.feign.client.resumeMatcher.dto;

import com.otbs.feign.client.resumeMatcher.entity.MatchResult;
import lombok.Data;
import java.util.List;

@Data
public class RoleEvaluationResponse {
    private String roleType;
    private double confidence;
    private String justification;
    private List<MatchResult.Criterion> adaptedCriteria;
}
