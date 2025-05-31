package com.otbs.medVisit.service;

import com.otbs.medVisit.dto.AppointmentRequestDTO;
import com.otbs.medVisit.dto.AppointmentResponseDTO;

import java.util.List;

public interface AppointmentService {
    void createAppointment(AppointmentRequestDTO appointment);

    void updateAppointment(AppointmentRequestDTO appointment, Long id);

    void deleteAppointment(Long id);
    void cancelAppointment(Long id);
    AppointmentResponseDTO getAppointmentById(Long id);
    List<AppointmentResponseDTO> getAllAppointments();
    List<AppointmentResponseDTO> getAppointmentsByPatientId(String patientId);
    List<AppointmentResponseDTO> getAppointmentsByMedVisitId(String medVisitId);

}
