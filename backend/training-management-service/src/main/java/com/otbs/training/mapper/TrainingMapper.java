package com.otbs.training.mapper;

import com.otbs.training.dto.InvitationResponseDTO;
import com.otbs.training.dto.TrainingRequestDTO;
import com.otbs.training.dto.TrainingResponseDTO;
import com.otbs.training.model.Training;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrainingMapper {

    private final InvitationMapper invitationMapper;

    public Training toEntity(TrainingRequestDTO dto) {
        return Training.builder()
                .title(dto.title())
                .description(dto.description())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .build();
    }

    public TrainingResponseDTO toResponseDTO(Training training) {
        return new TrainingResponseDTO(
                training.getId(),
                training.getTitle(),
                training.getDescription(),
                training.getDepartment(),
                training.getStartDate(),
                training.getEndDate(),
                training.getCreatedBy(),
                Optional.ofNullable(training.getInvitations())
                        .orElse(List.of())
                        .stream()
                        .map(invitationMapper::toResponseDTO)
                        .toList(),
                training.getCreatedAt()
        );
    }
}