package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.EJobOfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobOfferService {
    void createJobOffer(JobOfferRequestDTO jobOfferRequestDTO);
    void updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO);
    void deleteJobOffer(Long id);
    JobOfferResponseDTO getJobOfferById(Long id);
    List<JobOfferResponseDTO> getAllJobOffers();
    JobOfferResponseDTO toggleStatus(Long id, EJobOfferStatus status);
//    Page<JobOfferResponseDTO> getAllJobOffers(
//            String title,
//            String department,
//            String role,
//            Boolean isInternal,
//            String status,
//            Pageable pageable);

//    Page<JobOfferResponseDTO> getAllJobOffers(Pageable pageable);


}