package com.otbs.candidate.controller;

import com.otbs.candidate.service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.otbs.candidate.dto.CandidateRequestDTO;
import com.otbs.candidate.dto.CandidateResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<CandidateResponseDTO> addCandidate(@Valid @RequestBody CandidateRequestDTO candidateRequestDTO) {
        CandidateResponseDTO candidateResponseDTO = candidateService.addCandidate(candidateRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(candidateResponseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> updateCandidate(@PathVariable Long id, @Valid @RequestBody CandidateRequestDTO candidateRequestDTO) {
        CandidateResponseDTO updatedCandidate = candidateService.updateCandidate(id, candidateRequestDTO);
        return ResponseEntity.ok(updatedCandidate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidateById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateResponseDTO> getCandidate(@PathVariable Long id) {
        CandidateResponseDTO candidate = candidateService.getCandidateById(id);
        return ResponseEntity.ok(candidate);
    }

    @GetMapping
    public ResponseEntity<List<CandidateResponseDTO>> listCandidates() {
        List<CandidateResponseDTO> candidates = candidateService.listCandidates();
        return ResponseEntity.ok(candidates);
    }
}