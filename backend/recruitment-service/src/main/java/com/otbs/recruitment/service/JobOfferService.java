package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.EJobOfferStatus;

import java.util.List;

public interface JobOfferService {
    void createJobOffer(JobOfferRequestDTO jobOfferRequestDTO);
    void updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO);
    void deleteJobOffer(Long id);
    JobOfferResponseDTO getJobOfferById(Long id);
    List<JobOfferResponseDTO> getAllJobOffers();
    void toggleStatus(Long id, EJobOfferStatus status);

}