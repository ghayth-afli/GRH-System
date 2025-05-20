package com.otbs.feign.client.resumeMatcher;

import com.otbs.feign.client.resumeMatcher.dto.*;
import com.otbs.feign.client.resumeMatcher.dto.ParsedResumeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "resume-matcher", url = "http://localhost:5000",contextId ="resumeMatcherClient")
public interface ResumeMatcherClient {
    @PostMapping("/api/v1/match")
    ResponseEntity<MatchResponse> matchResume(@RequestBody MatchRequest request);

    @PostMapping("/api/v1/parse-resume")
    ResponseEntity<ParsedResumeResponse> parseResume(@RequestBody ResumeRequest request);

    @PostMapping("/api/v1/evaluate-role")
    ResponseEntity<RoleEvaluationResponse> evaluateRole(@RequestBody JobOfferRequestDTO request);
}
