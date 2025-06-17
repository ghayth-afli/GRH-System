package com.otbs.recruitment.service;

import com.otbs.feign.client.candidate.CandidateClient;
import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.resumeMatcher.ResumeMatcherClient;
import com.otbs.feign.client.user.dto.UserResponse;
import com.otbs.recruitment.dto.ApplicationDetailsResponseDTO;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.exception.ApplicationException;
import com.otbs.recruitment.exception.FileUploadException;
import com.otbs.recruitment.exception.JobOfferException;
import com.otbs.recruitment.exception.UserException;
import com.otbs.recruitment.mapper.ApplicationAttributesMapper;
import com.otbs.recruitment.model.*;
import com.otbs.recruitment.repository.InternalApplicationRepository;
import com.otbs.recruitment.repository.JobOfferRepository;
import com.otbs.recruitment.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternalApplicationServiceImpl implements ApplicationService {

    private final ResumeMatcherClient resumeMatcherClient;
    private final CandidateClient candidateClient;
    private final InternalApplicationRepository internalapplicationRepository;
    private final ApplicationAttributesMapper applicationAttributesMapper;
    private final AsyncProcessingService asyncProcessingService;

    private final JobOfferRepository jobOfferRepository;


    @Override
    public void createApplication(Long jobOfferId, MultipartFile resume) {
        // Validate if the job offer exists
        if (!jobOfferRepository.existsById(jobOfferId)) {
            throw new JobOfferException("Job offer not found");
        }

        // Validate if the resume is not empty
        if (resume.isEmpty()) {
            throw new ApplicationContextException("Resume cannot be empty");
        }

        // Get the resume base64
        String resumeBase64;
        String resumeType;
        byte[] resumeBytes;
        try {
            resumeBase64 = java.util.Base64.getEncoder().encodeToString(resume.getBytes());
            resumeBytes= resume.getBytes();
            resumeType = resume.getContentType().split("/")[1];
        } catch (IOException e) {
            throw new FileUploadException("Failed to process the resume file", e);
        }

        // Fetch job offer
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new JobOfferException("Job offer not found"));

        // Save the internal application
        InternalApplication internalApplication = InternalApplication.builder()
                .candidateId(null)
                .jobOffer(jobOffer)
                .matchResult(null)
                .status(EApplicationStatus.PENDING)
                .attachment(resumeBytes)
                .userId(getCurrentUser().id())
                .build();

        internalApplication = internalapplicationRepository.save(internalApplication);

        // Asynchronously parse resume and match resume with job offer
        asyncProcessingService.parseAndSaveResume(resumeBase64, resumeType, internalApplication.getId());
        asyncProcessingService.matchResumeAndSaveResult(jobOffer, resumeBase64, resumeType, internalApplication.getId());
        if (getCurrentUser().email() != null && !getCurrentUser().email().isEmpty()) {
            asyncProcessingService.sendMailNotification(
                    getCurrentUser().email(),
                    "Application Received",
                    "Your application for the job offer " + jobOffer.getTitle() + " has been received successfully."
            );
        }

    }

    @Override
    @Transactional
    public void deleteApplication(Long applicationId) {
        internalapplicationRepository.findByIdAndUserId(applicationId, getCurrentUser().id())
                .orElseThrow(() -> new ApplicationException("Application not found"));

        internalapplicationRepository.deleteById(applicationId);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDTO> getAllApplications(Long jobId) {
        var jobOffer = jobOfferRepository.findById(jobId)
                .orElseThrow(() -> new JobOfferException("Job offer not found"));

        return jobOffer.getInternalApplications().stream()
                .filter(application -> application.getMatchResult() != null && application.getCandidateId() != null)
                .map(application -> {
                    var candidate = candidateClient.getCandidate(application.getCandidateId()).getBody();
                    return applicationAttributesMapper.internalApplicationToResponseDTO(
                            application,
                            candidate.candidateInfo().name(),
                            candidate.candidateInfo().email(),
                            candidate.candidateInfo().phone()
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationDetailsResponseDTO getApplicationDetails(Long applicationId) {
        InternalApplication application = internalapplicationRepository.findById(applicationId).filter(
                        internalApplication ->internalApplication.getMatchResult() != null && internalApplication.getCandidateId() != null
                )
                .orElseThrow(() -> new ApplicationException("Application not found"));

        var candidate = candidateClient.getCandidate(application.getCandidateId()).getBody();

        return ApplicationDetailsResponseDTO.builder()
                .applicationId(application.getId())
                .resume(candidate)
                .attachment(application.getAttachment())
                .matchResult(application.getMatchResult())
                .build();
    }

    @Override
    @Transactional
    public void updateApplicationStatus(Long applicationId, EApplicationStatus status) {
        InternalApplication internalApplication = internalapplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationException("Application not found"));
        switch (status) {
            case SHORTLISTED -> internalApplication.setStatus(EApplicationStatus.SHORTLISTED);
            case SELECTED -> internalApplication.setStatus(EApplicationStatus.SELECTED);
            case REJECTED -> {
                internalApplication.setStatus(EApplicationStatus.REJECTED);
            }
            case HIRED -> {
                internalApplication.setStatus(EApplicationStatus.HIRED);
            }
        }
        internalapplicationRepository.save(internalApplication);
    }

    @Override
    @Transactional
    public void deleteApplicationByJobOfferId(Long jobOfferId) {
        InternalApplication c = internalapplicationRepository.findByJobOfferIdAndUserId(jobOfferId, getCurrentUser().id())
                .orElseThrow(() -> new ApplicationException("Application not found"));

        internalapplicationRepository.delete(c);
    }

    private UserResponse getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserResponse userResponse) {
            return userResponse;
        }
        throw new JobOfferException( String.format("Current user is not authenticated or does not have a valid user response: %s", principal));
    }

}
