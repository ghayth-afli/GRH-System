package com.otbs.recruitment.service;

import com.otbs.feign.client.candidate.CandidateClient;
import com.otbs.feign.client.candidate.dto.CandidateRequestDTO;
import com.otbs.feign.client.candidate.dto.CandidateResponseDTO_;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.resumeMatcher.ResumeMatcherClient;
import com.otbs.feign.client.resumeMatcher.dto.*;
import com.otbs.recruitment.exception.ApplicationException;
import com.otbs.recruitment.mapper.ApplicationAttributesMapper;
import com.otbs.recruitment.model.*;
import com.otbs.recruitment.repository.InternalApplicationRepository;
import com.otbs.recruitment.repository.JobOfferRepository;
import com.otbs.recruitment.repository.MatchResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncProcessingService {

    private final ResumeMatcherClient resumeMatcherClient;
    private final CandidateClient candidateClient;
    private final InternalApplicationRepository internalapplicationRepository;
    private final MatchResultRepository matchResultRepository;

    @Async("taskExecutor")
    public void parseAndSaveResume(String resumeBase64, String resumeType, Long internalApplicationId) {

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
        if (candidateResponseDTO == null) {
            throw new ApplicationException("Failed to save candidate");
        }
        //update the internal application with the candidate id
        InternalApplication internalApplication = internalapplicationRepository.findById(internalApplicationId)
                .orElseThrow(() -> new ApplicationException("Internal application not found"));

        internalApplication.setCandidateId(candidateResponseDTO.id());
        internalapplicationRepository.save(internalApplication);
    }

    @Async("taskExecutor")
    public void matchResumeAndSaveResult(JobOffer jobOffer, String resumeBase64, String resumeType, Long internalApplicationId) {
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
        InternalApplication internalApplication = internalapplicationRepository.findById(internalApplicationId)
                .orElseThrow(() -> new ApplicationException("Internal application not found"));
        internalApplication.setMatchResult(matchResult);
        internalapplicationRepository.save(internalApplication);
    }

    @Async("taskExecutor")
    public void fetchEmployeeAndNotify(String employeeId, EApplicationStatus status, String jobOfferTitle) {
        try {
            // Implementation from the original fetchEmployeeAndNotifyAsync method
        } catch (Exception e) {
            log.error("Error in async employee notification: {}", e.getMessage(), e);
        }
    }
}
