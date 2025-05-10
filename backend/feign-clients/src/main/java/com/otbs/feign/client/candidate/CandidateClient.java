package com.otbs.feign.client.candidate;


import com.otbs.feign.client.candidate.dto.CandidateRequestDTO;
import com.otbs.feign.client.candidate.dto.CandidateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "candidate-service", path = "http://localhost:8089",contextId ="candidateClient")
public interface CandidateClient {

    @PostMapping
    ResponseEntity<CandidateResponseDTO> addCandidate(@RequestBody CandidateRequestDTO candidateRequestDTO);

    @DeleteMapping("/{id}")
    void deleteCandidate(@PathVariable("id") Long id);

    @GetMapping("/{id}")
    ResponseEntity<CandidateResponseDTO> getCandidate(@PathVariable("id") Long id);

    @GetMapping
    ResponseEntity<List<CandidateResponseDTO>> listCandidates();
}
