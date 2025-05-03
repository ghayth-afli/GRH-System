package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.ApplicationResponseDTO;

import java.util.List;

public interface ApplicationService {
    void createApplication(Long jobOfferId, Long candidateId);
    void updateApplication(Long applicationId, Long jobOfferId, Long candidateId);
    void deleteApplication(Long applicationId);
    ApplicationResponseDTO getApplicationById(Long applicationId);
    List<ApplicationResponseDTO> getAllApplications();
}
