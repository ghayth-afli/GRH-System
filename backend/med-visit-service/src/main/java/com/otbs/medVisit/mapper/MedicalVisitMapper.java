package com.otbs.medVisit.mapper;

import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.model.MedicalVisit;
import org.springframework.stereotype.Service;


@Service
public class MedicalVisitMapper {
    public MedicalVisit toEntity(MedicalVisitRequestDTO medicalVisitRequestDTO) {
        return MedicalVisit.builder()
                .doctorName(medicalVisitRequestDTO.doctorName())
                .visitDate(medicalVisitRequestDTO.visitDate())
                .startTime(medicalVisitRequestDTO.startTime())
                .endTime(medicalVisitRequestDTO.endTime())
                .build();
    }

    public MedicalVisitResponseDTO toDto(MedicalVisit medicalVisit) {
        return MedicalVisitResponseDTO.builder()
                .id(medicalVisit.getId())
                .doctorName(medicalVisit.getDoctorName())
                .visitDate(medicalVisit.getVisitDate())
                .startTime(medicalVisit.getStartTime())
                .endTime(medicalVisit.getEndTime())
                .numberOfAppointments(
                    medicalVisit.getAppointments() != null ? medicalVisit.getAppointments().size() : 0
                )
                .build();
    }
}
