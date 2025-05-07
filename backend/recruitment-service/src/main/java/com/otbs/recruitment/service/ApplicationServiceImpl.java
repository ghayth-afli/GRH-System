package com.otbs.recruitment.service;

import com.otbs.recruitment.dto.ApplicationResponseDTO;

import java.util.List;

public class ApplicationServiceImpl implements ApplicationService {
    @Override
    public void createApplication(Long jobOfferId, Long candidateId) {

    }

    @Override
    public void updateApplication(Long applicationId, Long jobOfferId, Long candidateId) {

    }

    @Override
    public void deleteApplication(Long applicationId) {

    }

    @Override
    public ApplicationResponseDTO getApplicationById(Long applicationId) {
        return null;
    }

    @Override
    public List<ApplicationResponseDTO> getAllApplications() {
        return List.of();
    }
}
