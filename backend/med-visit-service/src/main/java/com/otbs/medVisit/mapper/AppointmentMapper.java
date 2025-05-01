package com.otbs.medVisit.mapper;

import com.otbs.medVisit.dto.AppointmentResponseDTO;
import com.otbs.medVisit.model.Appointment;
import org.springframework.stereotype.Service;

@Service
public class AppointmentMapper {

    public AppointmentResponseDTO toDto(Appointment appointment, String employeeFullName, String employeeEmail) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getMedicalVisit().getId(),
                appointment.getMedicalVisit().getDoctorName(),
                appointment.getTimeSlot(),
                appointment.getStatus(),
                employeeFullName,
                employeeEmail
        );
    }
}
