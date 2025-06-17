package com.otbs.training.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TrainingResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private Boolean isConfirmed;
    private Long totalInvitations;
}