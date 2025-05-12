package com.otbs.feign.client.candidate;


import com.otbs.feign.client.candidate.dto.CandidateRequestDTO;
import com.otbs.feign.client.candidate.dto.CandidateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "candidateClient", url = "http://localhost:8089")
public interface CandidateClient {

    @PostMapping("/api/v1/candidates")
    ResponseEntity<CandidateResponseDTO> addCandidate(@RequestBody CandidateRequestDTO candidateRequestDTO);

    @DeleteMapping("/api/v1/candidates/{id}")
    void deleteCandidate(@PathVariable("id") Long id);

    @GetMapping("/api/v1/candidates/{id}")
    ResponseEntity<CandidateResponseDTO> getCandidate(@PathVariable("id") Long id);

    @GetMapping("/api/v1/candidates")
    ResponseEntity<List<CandidateResponseDTO>> listCandidates();
}
