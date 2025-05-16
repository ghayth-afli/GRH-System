package com.otbs.recruitment.service;

import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.mapper.JobOfferAttributesMapper;
import com.otbs.recruitment.model.EJobOfferStatus;
import com.otbs.recruitment.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final JobOfferAttributesMapper jobOfferAttributesMapper;
    private final EmployeeClient employeeClient;

    @Override
    public void createJobOffer(JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferAttributesMapper.toEntity(jobOfferRequestDTO);
        jobOffer.setCreatedBy(getCurrentUserId());
        jobOfferRepository.save(jobOffer);
    }

    @Override
    public void updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Job offer not found or not created by the current user"));

        jobOffer.setTitle(jobOfferRequestDTO.title());
        jobOffer.setDescription(jobOfferRequestDTO.description());
        jobOffer.setDepartment(jobOfferRequestDTO.department());
        jobOffer.setResponsibilities(jobOfferRequestDTO.responsibilities());
        jobOffer.setQualifications(jobOfferRequestDTO.qualifications());
        jobOffer.setRole(jobOfferRequestDTO.role());
        jobOffer.setStatus(EJobOfferStatus.OPEN);
        jobOffer.setIsInternal(jobOfferRequestDTO.isInternal());

        jobOfferRepository.save(jobOffer);
    }

    @Override
    public void deleteJobOffer(Long id) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Job offer not found or not created by the current user"));
        jobOfferRepository.delete(jobOffer);
    }

@Override
public JobOfferResponseDTO getJobOfferById(Long id) {
    var jobOffer = jobOfferRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Job offer not found"));
    boolean isApplied = jobOffer.getInternalApplications().stream()
            .anyMatch(app -> app.getEmployeeId().equals(getCurrentUserId()));
    Integer numberOfApplications = getCurrentUserRole().equals("HR")
            ? jobOffer.getInternalApplications().size()
            : null;
    var responseDTO = jobOfferAttributesMapper.toResponseDTO(jobOffer);
    responseDTO.setNumberOfApplications(numberOfApplications);
    responseDTO.setApplied(isApplied);
    return responseDTO;
}

    @Override
    public List<JobOfferResponseDTO> getAllJobOffers() {
        return jobOfferRepository.findAll().stream()
                .map(jobOffer -> {
                    boolean isApplied = jobOffer.getInternalApplications().stream()
                            .anyMatch(app -> app.getEmployeeId().equals(getCurrentUserId()));
                    JobOfferResponseDTO responseDTO = jobOfferAttributesMapper.toResponseDTO(jobOffer);
                    responseDTO.setApplied(isApplied);
                    Integer numberOfApplications = getCurrentUserRole().equals("HR")
                            ? jobOffer.getInternalApplications().size()
                            : null;
                    responseDTO.setNumberOfApplications(numberOfApplications);
                    return responseDTO;
                })
                .toList();
    }

    @Override
    public JobOfferResponseDTO toggleStatus(Long id, EJobOfferStatus status) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Job offer not found or not created by the current user"));

        switch (status) {
            case OPEN -> jobOffer.setStatus(EJobOfferStatus.OPEN);
            case CLOSED -> jobOffer.setStatus(EJobOfferStatus.CLOSED);
            case CONVERTED_TO_EXTERNAL -> {
                jobOffer.setIsInternal(false);
            }
            case CONVERTED_TO_INTERNAL -> {
                jobOffer.setIsInternal(true);
            }
        }

        jobOfferRepository.save(jobOffer);
        return jobOfferAttributesMapper.toResponseDTO(jobOffer);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    private String getCurrentUserRole() {
        return employeeClient.getEmployeeByDn(getCurrentUserId()).role();
    }

//    @Override
//    public Page<JobOfferResponseDTO> getAllJobOffers(
//            String title,
//            String department,
//            String role,
//            Boolean isInternal,
//            String status,
//            Pageable pageable) {
//
//        // Create a specification for dynamic filtering
//        Specification<JobOffer> spec = Specification.where(null);
//
//        if (title != null && !title.isEmpty()) {
//            spec = spec.and((root, query, criteriaBuilder) ->
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
//        }
//
//        if (department != null && !department.isEmpty()) {
//            spec = spec.and((root, query, criteriaBuilder) ->
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("department")), "%" + department.toLowerCase() + "%"));
//        }
//
//        if (role != null && !role.isEmpty()) {
//            spec = spec.and((root, query, criteriaBuilder) ->
//                    criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + role.toLowerCase() + "%"));
//        }
//
//        if (isInternal != null) {
//            spec = spec.and((root, query, criteriaBuilder) ->
//                    criteriaBuilder.equal(root.get("isInternal"), isInternal));
//        }
//
//        if (status != null && !status.isEmpty()) {
//            spec = spec.and((root, query, criteriaBuilder) ->
//                    criteriaBuilder.equal(criteriaBuilder.lower(root.get("status")), status.toLowerCase()));
//        }
//
//        return jobOfferRepository.findAll(spec, pageable)
//                .map(jobOfferAttributesMapper::toResponseDTO);
//    }

//    @Override
//    public Page<JobOfferResponseDTO> getAllJobOffers(Pageable pageable) {
//        return jobOfferRepository.findAll(pageable)
//                .map(jobOfferAttributesMapper::toResponseDTO);
//    }


}