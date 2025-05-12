package com.otbs.feign.client.resumeMatcher.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.otbs.feign.client.resumeMatcher.entity.MatchResult;
import lombok.Data;


@Data
public class MatchResponse {

    @JsonProperty("match_result")
    private MatchResult matchResult;
}
