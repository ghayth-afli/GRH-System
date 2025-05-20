package com.otbs.recruitment.mapper;

import com.otbs.recruitment.dto.JobOfferRequestDTO;
import com.otbs.recruitment.dto.JobOfferResponseDTO;
import com.otbs.recruitment.model.JobOffer;
import com.otbs.recruitment.model.EJobOfferStatus;
import org.springframework.stereotype.Component;

@Component
public class JobOfferAttributesMapper {

    public JobOffer toEntity(JobOfferRequestDTO dto) {
        return JobOffer.builder()
                .title(dto.title())
                .description(dto.description())
                .department(dto.department())
                .responsibilities(dto.responsibilities())
                .qualifications(dto.qualifications())
                .role(dto.role())
                .status(EJobOfferStatus.OPEN)
                .isInternal(dto.isInternal())
                .build();
    }

    public JobOfferResponseDTO toResponseDTO(JobOffer entity) {
        return JobOfferResponseDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .department(entity.getDepartment())
                .responsibilities(entity.getResponsibilities())
                .qualifications(entity.getQualifications())
                .role(entity.getRole())
                .status(entity.getStatus())
                .isInternal(entity.getIsInternal())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

    }
}
