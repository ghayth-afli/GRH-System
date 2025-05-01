package com.otbs.medVisit.controller;


import com.otbs.medVisit.dto.AppointmentRequestDTO;
import com.otbs.medVisit.dto.AppointmentResponseDTO;
import com.otbs.medVisit.dto.MessageResponseDTO;
import com.otbs.medVisit.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    @GetMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(@PathVariable("id") Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByEmployeeId(@PathVariable("employeeId") String employeeId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(employeeId));
    }

    @GetMapping("/medVisit/{medVisitId}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByMedVisitId(@PathVariable("medVisitId") String medVisitId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByMedVisitId(medVisitId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> createAppointment(@Valid @RequestBody AppointmentRequestDTO appointmentRequestDTO) {
        appointmentService.createAppointment(appointmentRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> updateAppointment(@PathVariable("id") Long id, @Valid @RequestBody AppointmentRequestDTO appointmentRequestDTO) {
        appointmentService.updateAppointment(appointmentRequestDTO, id);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> deleteAppointment(@PathVariable("id") Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment deleted successfully"));
    }
}
