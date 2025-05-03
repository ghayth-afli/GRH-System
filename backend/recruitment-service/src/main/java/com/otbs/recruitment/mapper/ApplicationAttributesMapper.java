package com.otbs.recruitment.mapper;

import com.otbs.recruitment.dto.ApplicationRequestDTO;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.model.Application;
import com.otbs.recruitment.model.EApplicationStatus;
import com.otbs.recruitment.model.JobOffer;

public class ApplicationAttributesMapper {

    public Application toEntity(ApplicationRequestDTO dto, JobOffer jobOffer) {
        return Application.builder()
                .jobOffer(jobOffer)
                .applicantIdentifier(dto.applicantIdentifier())
                .applicantType(dto.applicantType())
                .resume(dto.resume())
                .coverLetter(dto.coverLetter())
                .status(EApplicationStatus.PENDING)
                .build();
    }

    public ApplicationResponseDTO toDto(Application application) {
        return new ApplicationResponseDTO(
                application.getId(),
                application.getJobOffer().getId(),
                application.getApplicantIdentifier(),
                application.getApplicantType(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
