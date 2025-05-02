package com.otbs.medVisit.service;

import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;

import java.util.List;

public interface MedicalVisitService {
    void createMedicalVisit(MedicalVisitRequestDTO medicalVisitRequestDTO);
    void updateMedicalVisit(MedicalVisitRequestDTO medicalVisitRequestDTO, Long medicalVisitId);
    void deleteMedicalVisit(Long id);
    MedicalVisitResponseDTO getMedicalVisit(Long id);
    List<MedicalVisitResponseDTO> getMedicalVisits();
}
