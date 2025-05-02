package com.otbs.medVisit.repository;

import com.otbs.medVisit.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    //findByPatientId
    List<Appointment> findByEmployeeId(String employeeId);
    List<Appointment> findByMedicalVisitId(Long medicalVisitId);
    boolean existsByEmployeeIdAndMedicalVisitId(String employeeId, Long medicalVisitId);
    boolean existsByTimeSlotAndMedicalVisitId(LocalDateTime timeSlot, Long medicalVisit_id);
    boolean existsByTimeSlotAndMedicalVisitIdAndIdNot(LocalDateTime timeSlot, Long medicalVisit_id, Long id);
}
