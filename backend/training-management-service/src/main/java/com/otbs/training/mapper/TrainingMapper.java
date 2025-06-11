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
        return TrainingResponseDTO.builder()
                .id(training.getId())
                .title(training.getTitle())
                .description(training.getDescription())
                .department(training.getDepartment())
                .startDate(training.getStartDate())
                .endDate(training.getEndDate())
                .createdBy(Optional.ofNullable(training.getCreatedBy()).orElse("System"))
                .createdAt(training.getCreatedAt())
                .isConfirmed(null)
                .totalInvitations(Optional.ofNullable(training.getInvitations())
                          .map(List::size)
                          .map(Integer::longValue)
                          .orElse(null))
                .build();
    }
}