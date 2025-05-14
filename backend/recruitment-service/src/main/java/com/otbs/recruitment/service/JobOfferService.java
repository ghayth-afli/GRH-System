package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobOfferService {
    JobOfferResponseDTO createJobOffer(JobOfferRequestDTO jobOfferRequestDTO);
    JobOfferResponseDTO updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO);
    void deleteJobOffer(Long id);
    JobOfferResponseDTO getJobOfferById(Long id);

    // Updated to include filters
    Page<JobOfferResponseDTO> getAllJobOffers(
            String title,
            String department,
            String role,
            Boolean isInternal,
            String status,
            Pageable pageable);

    // Simple pagination without filters
    Page<JobOfferResponseDTO> getAllJobOffers(Pageable pageable);

    // Keep the original method for backward compatibility if needed
    List<JobOfferResponseDTO> getAllJobOffers();
}