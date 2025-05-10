package com.otbs.candidate.service;

import com.otbs.candidate.dto.CandidateRequestDTO;
import com.otbs.candidate.dto.CandidateResponseDTO;
import com.otbs.candidate.mapper.CandidateAttributesMapper;
import com.otbs.candidate.model.Candidate;
import com.otbs.candidate.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateAttributesMapper candidateAttributesMapper;

    @Override
    public CandidateResponseDTO addCandidate(CandidateRequestDTO candidateRequestDTO) {
        Candidate candidate = candidateAttributesMapper.toEntity(candidateRequestDTO);
        Candidate savedCandidate = candidateRepository.save(candidate);
        return candidateAttributesMapper.toResponseDTO(savedCandidate);
    }

    @Override
    public CandidateResponseDTO updateCandidate(Long id, CandidateRequestDTO candidateRequestDTO) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        Candidate updatedCandidate = candidateAttributesMapper.toEntity(candidateRequestDTO);
        updatedCandidate.setId(id);
        candidateRepository.save(updatedCandidate);
        return candidateAttributesMapper.toResponseDTO(updatedCandidate);
    }

    @Override
    public void deleteCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        candidateRepository.delete(candidate);
    }

    @Override
    public CandidateResponseDTO getCandidateById(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));
        return candidateAttributesMapper.toResponseDTO(candidate);
    }

    @Override
    public List<CandidateResponseDTO> listCandidates() {
        List<Candidate> candidates = candidateRepository.findAll();
        return candidates.stream()
                .map(candidateAttributesMapper::toResponseDTO)
                .toList();
    }
}