package com.otbs.recruitment.service;

import com.otbs.feign.client.candidate.CandidateClient;
import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.resumeMatcher.ResumeMatcherClient;
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
    private final MatchResultRepository matchResultRepository;
    private final ApplicationAttributesMapper applicationAttributesMapper;
    private final RecruitmentNotificationService recruitmentNotificationService;
    private final AsyncProcessingService asyncProcessingService;

    private final JobOfferRepository jobOfferRepository;
    private final UserClient userClient;


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
                .userId(getCurrentUserId())
                .build();

        internalApplication = internalapplicationRepository.save(internalApplication);

        // Asynchronously parse resume and match resume with job offer
        asyncProcessingService.parseAndSaveResume(resumeBase64, resumeType, internalApplication.getId());
        asyncProcessingService.matchResumeAndSaveResult(jobOffer, resumeBase64, resumeType, internalApplication.getId());
    }

    @Override
    @Transactional
    public void deleteApplication(Long applicationId) {
        internalapplicationRepository.findByIdAndUserId(applicationId, getCurrentUserId())
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
                fetchUserAndNotifyAsync(
                        internalApplication.getUserId(),
                        EApplicationStatus.REJECTED,
                        internalApplication.getJobOffer().getTitle()
                );
            }
            case HIRED -> {
                internalApplication.setStatus(EApplicationStatus.HIRED);
                fetchUserAndNotifyAsync(
                        internalApplication.getUserId(),
                        EApplicationStatus.HIRED,
                        internalApplication.getJobOffer().getTitle()
                );
            }
        }
        internalapplicationRepository.save(internalApplication);
    }

    @Override
    @Transactional
    public void deleteApplicationByJobOfferId(Long jobOfferId) {
        InternalApplication c = internalapplicationRepository.findByJobOfferIdAndUserId(jobOfferId, getCurrentUserId())
                .orElseThrow(() -> new ApplicationException("Application not found"));

        internalapplicationRepository.delete(c);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getCurrentUserDepartment() {
        return userClient.getUserByDn(getCurrentUserId()).department();
    }


    public void fetchUserAndNotifyAsync(String userId, EApplicationStatus status,String jobOfferTitle) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return userClient.getUserByDn(userId);
            } catch (RuntimeException e) {
                log.error("Error fetching user details for ID {}: {}", userId, e.getMessage());
                throw new UserException("Error fetching user details");
            }
        }).thenAccept(user -> {
            switch (status) {
                case REJECTED -> {
                    String mailBody =
                    "Dear " + user.firstName()+" "+user.lastName() + ",\n\n"
                    + "Thank you for your interest in the" +jobOfferTitle +". "
                    + "After careful consideration, we regret to inform you that we have decided to move forward with other candidates at this time.\n\n"
                    + "We truly appreciate the time and effort you invested in applying and interviewing with us. "
                    + "Please do not hesitate to apply for future openings that match your skills and experience.\n\n"
                    + "We wish you the best of luck in your job search and future endeavors.\n\n"
                    + "Sincerely,\n"
                    + "Hr Team\n"
                    + "OTBS";
                    recruitmentNotificationService.sendMailNotification(
                            user.email(),
                            "Application Rejection",
                            mailBody
                    );
                }
                case HIRED -> {
                    String mailBody =
                            "Dear " + user.firstName()+" "+user.lastName() + ",\n\n"
                                    + "Congratulations! We are pleased to inform you that you have been selected for the position of" +jobOfferTitle +". "
                                    + "We were impressed with your skills and experience, and we believe you will be a valuable addition to our team.\n\n"
                                    + "Please find attached the offer letter for your review. If you have any questions or need further information, "
                                    + "please do not hesitate to reach out.\n\n"
                                    + "We look forward to welcoming you to our team!\n\n"
                                    + "Best regards,\n"
                                    + "Hr Team\n"
                                    + "OTBS";
                    recruitmentNotificationService.sendMailNotification(
                            user.email(),
                            "Application Acceptance",
                            mailBody
                    );
                }
            }
        }).exceptionally(throwable -> {
            log.error("Failed to send notification for user {}: {}", userId, throwable.getMessage());
            return null;
        });
    }

}
