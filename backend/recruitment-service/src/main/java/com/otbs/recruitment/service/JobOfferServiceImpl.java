package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.mapper.JobOfferAttributesMapper;
import com.otbs.recruitment.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final JobOfferAttributesMapper jobOfferAttributesMapper;

    @Override
    public JobOfferResponseDTO createJobOffer(JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferAttributesMapper.toEntity(jobOfferRequestDTO);
        jobOffer.setCreatedBy(this.getCurrentUserId());
        jobOfferRepository.save(jobOffer);
        return jobOfferAttributesMapper.toResponseDTO(jobOffer);
    }

    @Override
    public JobOfferResponseDTO updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id,this.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Job offer not found or not created by the current user"));
        jobOffer.setTitle(jobOfferRequestDTO.title());
        jobOffer.setDescription(jobOfferRequestDTO.description());
        jobOffer.setDepartment(jobOfferRequestDTO.department());
        jobOffer.setResponsibilities(jobOfferRequestDTO.responsibilities());
        jobOffer.setQualifications(jobOfferRequestDTO.qualifications());
        jobOffer.setRole(jobOfferRequestDTO.role());
        jobOffer.setStatus(jobOffer.getStatus());
        jobOffer.setIsInternal(jobOfferRequestDTO.isInternal());
        jobOfferRepository.save(jobOffer);
        return jobOfferAttributesMapper.toResponseDTO(jobOffer);
    }

    @Override
    public void deleteJobOffer(Long id) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, this.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Job offer not found or not created by the current user"));
        jobOfferRepository.delete(jobOffer);
    }

    @Override
    public JobOfferResponseDTO getJobOfferById(Long id) {
        var jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job offer not found"));
        return jobOfferAttributesMapper.toResponseDTO(jobOffer);
    }

    @Override
    public List<JobOfferResponseDTO> getAllJobOffers() {
        var jobOffers = jobOfferRepository.findAll();
        return jobOffers.stream()
                .map(jobOfferAttributesMapper::toResponseDTO)
                .toList();
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
