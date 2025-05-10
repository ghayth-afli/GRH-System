package com.otbs.candidate.service;

import com.otbs.candidate.dto.CandidateRequestDTO;
import com.otbs.candidate.dto.CandidateResponseDTO;

import java.util.List;

public interface CandidateService {

    CandidateResponseDTO addCandidate(CandidateRequestDTO candidateRequestDTO);

    CandidateResponseDTO updateCandidate(Long id, CandidateRequestDTO candidateRequestDTO);

    void deleteCandidateById(Long id);

    CandidateResponseDTO getCandidateById(Long id);

    List<CandidateResponseDTO> listCandidates();
}