package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;

import java.util.List;

public interface JobOfferService {

    void createJobOffer(JobOfferRequestDTO jobOfferRequestDTO);
    void updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO);
    void deleteJobOffer(Long id);
    JobOfferRequestDTO getJobOfferById(Long id);
    List<JobOfferRequestDTO> getAllJobOffers();
}
