package com.otbs.recruitment.dto;

import com.otbs.recruitment.model.EJobOfferStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
public class JobOfferResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String department;
    private String responsibilities;
    private String qualifications;
    private String role;
    private boolean isApplied;
    private Integer numberOfApplications;
    private EJobOfferStatus status;
    private Boolean isInternal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}