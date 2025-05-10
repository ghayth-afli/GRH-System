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
                .status(EJobOfferStatus.valueOf(dto.status()))
                .isInternal(dto.isInternal())
                .build();
    }

    public JobOfferResponseDTO toResponseDTO(JobOffer entity) {
        return new JobOfferResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDepartment(),
                entity.getResponsibilities(),
                entity.getQualifications(),
                entity.getRole(),
                entity.getStatus(),
                entity.getIsInternal(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
