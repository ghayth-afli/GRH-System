package com.otbs.training.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otbs.training.model.EStatus;

public record InvitationResponseDTO (
    Long id,
    String userName,
    EStatus status,
    String userId
){
}
