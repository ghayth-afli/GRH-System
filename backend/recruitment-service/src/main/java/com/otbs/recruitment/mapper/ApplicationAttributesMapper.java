package com.otbs.recruitment.mapper;

import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.model.InternalApplication;
import org.springframework.stereotype.Component;

@Component
public class ApplicationAttributesMapper {

    //internalApplicationToResponseDTO
    public ApplicationResponseDTO internalApplicationToResponseDTO(InternalApplication entity, String fullName,String email,String phone) {
        return ApplicationResponseDTO.builder()
                .id(entity.getId())
                .candidateId(entity.getId())
                .FullName(fullName)
                .isInternal(true)
                .Email(email)
                .Phone(phone)
                .status(entity.getStatus())
                .score(entity.getMatchResult().getScore())
                .submissionDate(entity.getCreatedAt())
                .build();
    }
}
