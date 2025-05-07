package com.otbs.recruitment.mapper;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.JobOffer;

public class JobOfferAttributesMapper {

    public JobOffer toEntity(JobOfferRequestDTO dto) {
        return JobOffer.builder()
                .title(dto.title())
                .description(dto.description())
                .department(dto.department())
                .role(dto.role())
                .status(dto.status())
                .isInternal(dto.isInternal())
                .build();
    }

    public JobOfferResponseDTO toDto(JobOffer jobOffer) {
        return new JobOfferResponseDTO(
                jobOffer.getId(),
                jobOffer.getTitle(),
                jobOffer.getDescription(),
                jobOffer.getDepartment(),
                jobOffer.getRole(),
                jobOffer.getStatus(),
                jobOffer.getIsInternal(),
                jobOffer.getCreatedAt()
        );
    }
}
