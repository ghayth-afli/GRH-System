package com.otbs.training.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otbs.training.model.EStatus;

public record InvitationResponseDTO (
    Long id,
    String employeeName,
    EStatus status,
    @JsonIgnore
    TrainingResponseDTO training
){
}
