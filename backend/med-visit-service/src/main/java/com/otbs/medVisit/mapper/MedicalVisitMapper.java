package com.otbs.medVisit.mapper;

import com.otbs.medVisit.dto.MedicalVisitRequestDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.model.MedicalVisit;
import org.springframework.stereotype.Service;


@Service
public class MedicalVisitMapper {
    public MedicalVisit toEntity(MedicalVisitRequestDTO medicalVisitRequestDTO) {
        return new MedicalVisit(
                medicalVisitRequestDTO.doctorName(),
                medicalVisitRequestDTO.visitDate(),
                medicalVisitRequestDTO.startTime(),
                medicalVisitRequestDTO.endTime()
        );
    }

    public MedicalVisitResponseDTO toDto(MedicalVisit medicalVisit) {
        return new MedicalVisitResponseDTO(
                medicalVisit.getId(),
                medicalVisit.getDoctorName(),
                medicalVisit.getVisitDate(),
                medicalVisit.getStartTime(),
                medicalVisit.getEndTime(),
                medicalVisit.getAppointments().size()
        );
    }
}
