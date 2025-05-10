package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;

import java.util.List;

public interface JobOfferService {

    JobOfferResponseDTO createJobOffer(JobOfferRequestDTO jobOfferRequestDTO);
    JobOfferResponseDTO updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO);
    void deleteJobOffer(Long id);
    JobOfferResponseDTO getJobOfferById(Long id);
    List<JobOfferResponseDTO> getAllJobOffers();
}
