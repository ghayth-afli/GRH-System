package com.otbs.recruitment.service;

import com.otbs.feign.client.candidate.CandidateClient;
import com.otbs.feign.client.candidate.dto.CandidateRequestDTO;
import com.otbs.feign.client.candidate.dto.CandidateResponseDTO_;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.resumeMatcher.ResumeMatcherClient;
import com.otbs.feign.client.resumeMatcher.dto.*;
import com.otbs.recruitment.dto.ApplicationDetailsResponseDTO;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.mapper.ApplicationAttributesMapper;
import com.otbs.recruitment.model.*;
import com.otbs.recruitment.repository.InternalApplicationRepository;
import com.otbs.recruitment.repository.JobOfferRepository;
import com.otbs.recruitment.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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

    private final JobOfferRepository jobOfferRepository;
    private final EmployeeClient employeeClient;

    @Override
    public void createApplication(Long jobOfferId, MultipartFile resume) {
        //validate if the job offer exists
        if (!jobOfferRepository.existsById(jobOfferId)) {
            throw new IllegalArgumentException("Job offer not found");
        }

        //validate if the resume is not empty
        if (resume.isEmpty()) {
            throw new IllegalArgumentException("Resume cannot be empty");
        }

        //get the resume base64
        String resumeBase64;
        String resumeType ;
        try {
            resumeBase64 = java.util.Base64.getEncoder().encodeToString(resume.getBytes());
            resumeType = resume.getContentType().split("/")[1];
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the resume file", e);
        }

        //parse the resume
        ResumeRequest resumeRequest = ResumeRequest.builder()
                .resume(resumeBase64)
                .resumeType(resumeType)
                .build();

        ParsedResumeResponse parsedResumeResponse = resumeMatcherClient.parseResume(resumeRequest).getBody();


        //save the resume to candidate service
        CandidateRequestDTO candidateRequestDTO = CandidateRequestDTO.builder()
                .candidateInfo(parsedResumeResponse.getCandidateInfo())
                .certifications(parsedResumeResponse.getCertifications())
                .education(parsedResumeResponse.getEducation())
                .experience(parsedResumeResponse.getExperience())
                .languages(parsedResumeResponse.getLanguages())
                .projects(parsedResumeResponse.getProjects())
                .skills(parsedResumeResponse.getSkills())
                .build();


        CandidateResponseDTO_ candidateResponseDTO = candidateClient.addCandidate(candidateRequestDTO).getBody();


        //match resume with job offer
        JobOffer jobOffer = jobOfferRepository.findById(jobOfferId)
                .orElseThrow(() -> new IllegalArgumentException("Job offer not found"));

        JobOfferRequestDTO jobOfferRequestClientDTO = JobOfferRequestDTO.builder()
                .title(jobOffer.getTitle())
                .description(jobOffer.getDescription())
                .role(jobOffer.getRole())
                .isInternal(jobOffer.getIsInternal())
                .department(jobOffer.getDepartment())
                .responsibilities(jobOffer.getResponsibilities())
                .qualifications(jobOffer.getQualifications())
                .build();

        MatchRequest matchRequest = MatchRequest.builder()
                .resume(resumeBase64)
                .resumeType(resumeType)
                .jobDescription(jobOfferRequestClientDTO)
                .build();

        MatchResponse matchResponse = resumeMatcherClient.matchResume(matchRequest).getBody();


        //save the internal application
        MatchResult matchResult = MatchResult.builder()
                .score(matchResponse.getMatchResult().getScore())
                .rawScore(matchResponse.getMatchResult().getRawScore())
                .interpretation(matchResponse.getMatchResult().getInterpretation())
                .details(   MatchScoreDetails.builder()
                            .skillsMatch(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getSkillsMatch().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getSkillsMatch().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getSkillsMatch().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getSkillsMatch().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getSkillsMatch().getAnalysis())
                                            .build()
                            )
                            .relevantExperience(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getRelevantExperience().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getRelevantExperience().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getRelevantExperience().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getRelevantExperience().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getRelevantExperience().getAnalysis())
                                            .build()
                            )
                            .education(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getEducation().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getEducation().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getEducation().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getEducation().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getEducation().getAnalysis())
                                            .build()
                            )
                            .certifications(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getCertifications().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getCertifications().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getCertifications().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getCertifications().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getCertifications().getAnalysis())
                                            .build()
                            )
                            .culturalFit(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getCulturalFit().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getCulturalFit().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getCulturalFit().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getCulturalFit().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getCulturalFit().getAnalysis())
                                            .build()
                            )
                            .languageProficiency(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getLanguageProficiency().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getLanguageProficiency().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getLanguageProficiency().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getLanguageProficiency().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getLanguageProficiency().getAnalysis())
                                            .build()
                            )
                            .achievementsProjects(
                                    MatchEvaluationDetail.builder()
                                            .rawScore(matchResponse.getMatchResult().getDetails().getAchievementsProjects().getRawScore())
                                            .weightedScore(matchResponse.getMatchResult().getDetails().getAchievementsProjects().getWeightedScore())
                                            .matchingSkills(matchResponse.getMatchResult().getDetails().getAchievementsProjects().getMatchingSkills())
                                            .missingSkills(matchResponse.getMatchResult().getDetails().getAchievementsProjects().getMissingSkills())
                                            .analysis(matchResponse.getMatchResult().getDetails().getAchievementsProjects().getAnalysis()
                            )
                            .build()
                        )
                        .build()
                )
                .redFlags(matchResponse.getMatchResult().getRedFlags())
                .bonusPoints(matchResponse.getMatchResult().getBonusPoints())
                .roleType(matchResponse.getMatchResult().getRoleType())
                .roleConfidence(matchResponse.getMatchResult().getRoleConfidence())
                .adaptedCriteria(
                        matchResponse.getMatchResult().getAdaptedCriteria().stream()
                                .map(criterion -> MatchResult.Criterion.builder()
                                        .name(criterion.getName())
                                        .weight(criterion.getWeight())
                                        .build())
                                .toList()
                )
                .roleSpecificInsights(matchResponse.getMatchResult().getRoleSpecificInsights())
                .build();



        matchResult=matchResultRepository.save(matchResult);
        InternalApplication internalApplication = InternalApplication.builder()
                .candidateId(candidateResponseDTO.id())
                .jobOffer(jobOffer)
                .matchResult(matchResult)
                .status(EApplicationStatus.PENDING)
                .employeeId(getCurrentUserId())
                .build();

        internalapplicationRepository.save(internalApplication);

    }

    @Override
    public void deleteApplication(Long applicationId) {
        internalapplicationRepository.findByIdAndEmployeeId(applicationId, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        internalapplicationRepository.deleteById(applicationId);
    }


    @Override
    public List<ApplicationResponseDTO> getAllApplications(Long jobId) {
        var jobOffer = jobOfferRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job offer not found"));

        return jobOffer.getInternalApplications().stream()
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
    public ApplicationDetailsResponseDTO getApplicationDetails(Long applicationId) {
        InternalApplication application = internalapplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        var candidate = candidateClient.getCandidate(application.getCandidateId()).getBody();

        return ApplicationDetailsResponseDTO.builder()
                .applicationId(application.getId())
                .resume(candidate)
                .matchResult(application.getMatchResult())
                .build();
    }

    @Override
    public void updateApplicationStatus(Long applicationId, EApplicationStatus status) {
        InternalApplication internalApplication = internalapplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        switch (status) {
            case SHORTLISTED -> internalApplication.setStatus(EApplicationStatus.SHORTLISTED);
            case SELECTED -> internalApplication.setStatus(EApplicationStatus.SELECTED);
            case REJECTED -> {
                internalApplication.setStatus(EApplicationStatus.REJECTED);
                fetchEmployeeAndNotifyAsync(
                        internalApplication.getEmployeeId(),
                        EApplicationStatus.REJECTED,
                        internalApplication.getJobOffer().getTitle()
                );
            }
            case HIRED -> {
                internalApplication.setStatus(EApplicationStatus.HIRED);
                fetchEmployeeAndNotifyAsync(
                        internalApplication.getEmployeeId(),
                        EApplicationStatus.HIRED,
                        internalApplication.getJobOffer().getTitle()
                );
            }
        }
        internalapplicationRepository.save(internalApplication);
    }

    @Override
    public void deleteApplicationByJobOfferId(Long jobOfferId) {
        InternalApplication c = internalapplicationRepository.findByJobOfferIdAndEmployeeId(jobOfferId, getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        internalapplicationRepository.delete(c);
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String getCurrentUserDepartment() {
        return employeeClient.getEmployeeByDn(getCurrentUserId()).department();
    }

    private String getCurrentUserRole() {
        return employeeClient.getEmployeeByDn(getCurrentUserId()).role();
    }

    @Async
    protected void fetchEmployeeAndNotifyAsync(String employeeId, EApplicationStatus status,String jobOfferTitle) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return employeeClient.getEmployeeByDn(employeeId);
            } catch (RuntimeException e) {
                log.error("Error fetching employee details for ID {}: {}", employeeId, e.getMessage());
                throw new RuntimeException("Error fetching employee details");
            }
        }).thenAccept(employee -> {
            switch (status) {
                case REJECTED -> {
                    String mailBody =
                    "Dear " + employee.firstName()+" "+employee.lastName() + ",\n\n"
                    + "Thank you for your interest in the" +jobOfferTitle +". "
                    + "After careful consideration, we regret to inform you that we have decided to move forward with other candidates at this time.\n\n"
                    + "We truly appreciate the time and effort you invested in applying and interviewing with us. "
                    + "Please do not hesitate to apply for future openings that match your skills and experience.\n\n"
                    + "We wish you the best of luck in your job search and future endeavors.\n\n"
                    + "Sincerely,\n"
                    + "Hr Team\n"
                    + "OTBS";
                    recruitmentNotificationService.sendMailNotification(
                            employee.email(),
                            "Application Rejection",
                            mailBody
                    );
                }
                case HIRED -> {
                    String mailBody =
                            "Dear " + employee.firstName()+" "+employee.lastName() + ",\n\n"
                                    + "Congratulations! We are pleased to inform you that you have been selected for the position of" +jobOfferTitle +". "
                                    + "We were impressed with your skills and experience, and we believe you will be a valuable addition to our team.\n\n"
                                    + "Please find attached the offer letter for your review. If you have any questions or need further information, "
                                    + "please do not hesitate to reach out.\n\n"
                                    + "We look forward to welcoming you to our team!\n\n"
                                    + "Best regards,\n"
                                    + "Hr Team\n"
                                    + "OTBS";
                    recruitmentNotificationService.sendMailNotification(
                            employee.email(),
                            "Application Acceptance",
                            mailBody
                    );
                }
            }
        }).exceptionally(throwable -> {
            log.error("Failed to send notification for employee {}: {}", employeeId, throwable.getMessage());
            return null;
        });
    }
}
