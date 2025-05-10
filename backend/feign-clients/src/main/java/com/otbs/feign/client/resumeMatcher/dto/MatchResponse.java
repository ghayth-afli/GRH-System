package com.otbs.feign.client.resumeMatcher.dto;

import com.otbs.feign.client.resumeMatcher.entity.MatchResult;
import lombok.Data;


@Data
public class MatchResponse {

    private MatchResult matchResult;
}
