package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.ApplicationDetailsResponseDTO;
import com.otbs.recruitment.dto.ApplicationResponseDTO;
import com.otbs.recruitment.model.EApplicationStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ApplicationService {
    void createApplication(Long jobOfferId,  MultipartFile attachment);
    void deleteApplication(Long applicationId);
    ApplicationResponseDTO getApplicationById(Long applicationId);
    List<ApplicationResponseDTO> getAllApplications(Long jobId);
    ApplicationDetailsResponseDTO getApplicationDetails(Long applicationId);
    void updateApplicationStatus(Long applicationId, EApplicationStatus status);
}