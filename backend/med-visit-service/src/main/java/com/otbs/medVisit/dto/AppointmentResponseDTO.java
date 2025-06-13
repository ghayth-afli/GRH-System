package com.otbs.medVisit.dto;

import com.otbs.medVisit.model.EAppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponseDTO(
    Long id,
    Long medicalVisitId,
    String doctorName,
    LocalDateTime timeSlot,
    EAppointmentStatus status,
    String userFullName,
    String userEmail
){}
