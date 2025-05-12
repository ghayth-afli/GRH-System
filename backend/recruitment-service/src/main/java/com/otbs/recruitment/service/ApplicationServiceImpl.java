package com.otbs.recruitment.service;

import com.otbs.feign.client.candidate.CandidateClient;
import com.otbs.feign.client.candidate.dto.CandidateRequestDTO;
import com.otbs.feign.client.candidate.dto.CandidateResponseDTO;
import com.otbs.feign.client.resumeMatcher.ResumeMatcherClient;
import com.otbs.feign.client.resumeMatcher.dto.*;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.model.*;
import com.otbs.recruitment.repository.InternalApplicationRepository;
import com.otbs.recruitment.repository.JobOfferRepository;
import com.otbs.recruitment.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ResumeMatcherClient resumeMatcherClient;
    private final CandidateClient candidateClient;
    private final InternalApplicationRepository internalapplicationRepository;
    private final MatchResultRepository matchResultRepository;

    private final JobOfferRepository jobOfferRepository;

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


        CandidateResponseDTO candidateResponseDTO = candidateClient.addCandidate(candidateRequestDTO).getBody();


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
    public ApplicationResponseDTO getApplicationById(Long applicationId) {
        return null;
    }

    @Override
    public List<ApplicationResponseDTO> getAllApplications() {
        return List.of();
    }

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
