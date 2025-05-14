package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.mapper.JobOfferAttributesMapper;
import com.otbs.recruitment.model.JobOffer;
import com.otbs.recruitment.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public Page<JobOfferResponseDTO> getAllJobOffers(
            String title,
            String department,
            String role,
            Boolean isInternal,
            String status,
            Pageable pageable) {

        // Create a specification for dynamic filtering
        Specification<JobOffer> spec = Specification.where(null);

        if (title != null && !title.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        }

        if (department != null && !department.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), "%" + department.toLowerCase() + "%"));
        }

        if (role != null && !role.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + role.toLowerCase() + "%"));
        }

        if (isInternal != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("isInternal"), isInternal));
        }

        if (status != null && !status.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.toLowerCase()));
        }

        return jobOfferRepository.findAll(spec, pageable)
                .map(jobOfferAttributesMapper::toResponseDTO);
    }

    @Override
    public Page<JobOfferResponseDTO> getAllJobOffers(Pageable pageable) {
        return jobOfferRepository.findAll(pageable)
                .map(jobOfferAttributesMapper::toResponseDTO);
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