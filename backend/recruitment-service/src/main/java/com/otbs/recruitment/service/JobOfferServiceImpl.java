package com.otbs.recruitment.service;

import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.exception.JobOfferException;
import com.otbs.recruitment.mapper.JobOfferAttributesMapper;
import com.otbs.recruitment.model.EApplicationStatus;
import com.otbs.recruitment.model.EJobOfferStatus;
import com.otbs.recruitment.model.InternalApplication;
import com.otbs.recruitment.repository.JobOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class JobOfferServiceImpl implements JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final JobOfferAttributesMapper jobOfferAttributesMapper;
    private final EmployeeClient employeeClient;
    private final ApplicationService applicationService;

    @Override
    public void createJobOffer(JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferAttributesMapper.toEntity(jobOfferRequestDTO);
        jobOffer.setCreatedBy(getCurrentUserId());
        jobOfferRepository.save(jobOffer);
    }

    @Override
    public void updateJobOffer(Long id, JobOfferRequestDTO jobOfferRequestDTO) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, getCurrentUserId())
                .orElseThrow(() -> new JobOfferException("Job offer not found or not created by the current user"));

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
                .orElseThrow(() -> new JobOfferException("Job offer not found or not created by the current user"));
        jobOfferRepository.delete(jobOffer);
    }

    @Override
    public JobOfferResponseDTO getJobOfferById(Long id) {
        var jobOffer = jobOfferRepository.findById(id)
                .orElseThrow(() -> new JobOfferException("Job offer not found"));

        String currentUserId = getCurrentUserId();
        String currentUserRole = getCurrentUserRole();

        boolean isApplied = jobOffer.getInternalApplications().stream()
                .anyMatch(app -> app.getEmployeeId().equals(currentUserId));

        Integer numberOfApplications = "HR".equals(currentUserRole)
                ? jobOffer.getInternalApplications().size()
                : null;

        EApplicationStatus applicationStatus = !"HR".equals(currentUserRole)
                ? jobOffer.getInternalApplications().stream()
                .filter(app -> app.getEmployeeId().equals(currentUserId))
                .map(InternalApplication::getStatus)
                .findFirst()
                .orElse(null)
                : null;

        var responseDTO = jobOfferAttributesMapper.toResponseDTO(jobOffer);
        responseDTO.setNumberOfApplications(numberOfApplications);
        responseDTO.setApplied(isApplied);
        responseDTO.setApplicationStatus(applicationStatus);

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
                    EApplicationStatus applicationStatus = !getCurrentUserRole().equals("HR")
                            ? jobOffer.getInternalApplications().stream()
                            .filter(app -> app.getEmployeeId().equals(getCurrentUserId()))
                            .map(InternalApplication::getStatus)
                            .findFirst()
                            .orElse(null)
                            : null;
                    responseDTO.setNumberOfApplications(numberOfApplications);
                    responseDTO.setApplicationStatus(applicationStatus);
                    return responseDTO;
                })
                .toList();
    }

    @Override
    public void toggleStatus(Long id, EJobOfferStatus status) {
        var jobOffer = jobOfferRepository.findByIdAndCreatedBy(id, getCurrentUserId())
                .orElseThrow(() -> new JobOfferException("Job offer not found or not created by the current user"));

        switch (status) {
            case OPEN -> jobOffer.setStatus(EJobOfferStatus.OPEN);
            case CLOSED -> {
                jobOffer.setStatus(EJobOfferStatus.CLOSED);
                jobOffer.getInternalApplications().forEach(application -> {
                    if(application.getStatus().equals(EApplicationStatus.PENDING)){
                        applicationService.updateApplicationStatus(application.getId(), EApplicationStatus.REJECTED);
                    }
                });
            }
            case CONVERTED_TO_EXTERNAL -> {
                jobOffer.setIsInternal(false);
            }
            case CONVERTED_TO_INTERNAL -> {
                jobOffer.setIsInternal(true);
            }
        }

        jobOfferRepository.save(jobOffer);
        jobOfferAttributesMapper.toResponseDTO(jobOffer);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
    private String getCurrentUserRole() {
        return employeeClient.getEmployeeByDn(getCurrentUserId()).role();
    }
}