package com.otbs.medVisit.service;

import com.otbs.common.event.Event;
import com.otbs.feign.client.employee.EmployeeClient;
import com.otbs.feign.client.employee.dto.EmployeeResponse;
import com.otbs.medVisit.dto.AppointmentRequestDTO;
import com.otbs.medVisit.dto.AppointmentResponseDTO;
import com.otbs.medVisit.dto.MedicalVisitResponseDTO;
import com.otbs.medVisit.exception.AppointmentException;
import com.otbs.medVisit.exception.MedicalVisitException;
import com.otbs.medVisit.exception.TimeSlotNotAvailableException;
import com.otbs.medVisit.mapper.AppointmentMapper;
import com.otbs.medVisit.mapper.MedicalVisitMapper;
import com.otbs.medVisit.model.Appointment;
import com.otbs.medVisit.model.EAppointmentStatus;
import com.otbs.medVisit.repository.AppointmentRepository;
import com.otbs.medVisit.repository.MedicalVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final String HR_ROLE = "HR";

    private final AppointmentRepository appointmentRepository;
    private final MedicalVisitService medicalVisitService;
    private final MedicalVisitNotificationService medicalVisitNotificationService;
    private final MedicalVisitRepository medicalVisitRepository;
    private final AppointmentMapper appointmentMapper;
    private final EmployeeClient employeeClient;
    private final MedicalVisitMapper medicalVisitMapper;
    private final MedicalVisitNotificationEventService notificationEventService;

    @Override
    @Transactional
    public void createAppointment(AppointmentRequestDTO appointment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String employeeId = authentication.getName();

        // Check for existing appointment
        if (appointmentRepository.existsByEmployeeIdAndMedicalVisitId(employeeId, appointment.medicalVisitId())) {
            throw new AppointmentException("You already have an appointment for this medical visit");
        }

        // Validate medical visit
        MedicalVisitResponseDTO medicalVisit = medicalVisitService.getMedicalVisit(appointment.medicalVisitId());
        if (medicalVisit.getVisitDate().isBefore(LocalDate.now())) {
            throw new MedicalVisitException("Medical visit is not available");
        }

        // Validate time slot
        validateTimeSlot(appointment, medicalVisit);

        // Check time slot availability
        if (appointmentRepository.existsByTimeSlotAndMedicalVisitId(appointment.timeSlot(), appointment.medicalVisitId())) {
            throw new TimeSlotNotAvailableException("Time slot not available");
        }

        // Create and save appointment
        medicalVisitRepository.findById(appointment.medicalVisitId()).ifPresentOrElse(visit -> {
            Appointment newAppointment = Appointment.builder()
                    .employeeId(employeeId)
                    .medicalVisit(visit)
                    .timeSlot(appointment.timeSlot())
                    .status(EAppointmentStatus.PLANIFIE)
                    .build();
            appointmentRepository.save(newAppointment);

            // Asynchronously send notification
            sendNotificationAsync(employeeId, "Appointment created",
                    "Your appointment has been created for " + appointment.timeSlot());
        }, () -> {
            throw new AppointmentException("Medical visit not found");
        });

        sendAsyncEventNotifications(medicalVisit, "UPDATED_MEDICAL_VISIT");

    }

    @Override
    @Transactional
    public void updateAppointment(AppointmentRequestDTO appointmentRequestDTO, Long appointmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateAppointmentAccess(appointmentId, authentication);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Appointment not found"));

        // Validate time slot
        MedicalVisitResponseDTO medicalVisit = medicalVisitService.getMedicalVisit(appointmentRequestDTO.medicalVisitId());
        validateTimeSlot(appointmentRequestDTO, medicalVisit);

        // Check time slot availability (exclude current appointment)
        if (appointmentRepository.existsByTimeSlotAndMedicalVisitIdAndIdNot(
                appointmentRequestDTO.timeSlot(), appointmentRequestDTO.medicalVisitId(), appointmentId)) {
            throw new TimeSlotNotAvailableException("Time slot not available");
        }

        // Update and save
        appointment.setTimeSlot(appointmentRequestDTO.timeSlot());
        appointmentRepository.save(appointment);

        // Asynchronously send notification
        sendNotificationAsync(appointment.getEmployeeId(), "Appointment updated",
                "Your appointment has been updated to " + appointmentRequestDTO.timeSlot());
        sendAsyncEventNotifications(medicalVisit, "UPDATED_MEDICAL_VISIT");
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateAppointmentAccess(id, authentication);

        if (!appointmentRepository.existsById(id)) {
            throw new AppointmentException("Appointment not found");
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    public void cancelAppointment(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Appointment appointment = appointmentRepository.findByMedicalVisitIdAndEmployeeId(id, authentication.getName())
                .orElseThrow(() -> new AppointmentException("Appointment not found"));

        appointmentRepository.deleteById(appointment.getId());
        sendNotificationAsync(appointment.getEmployeeId(), "Appointment cancelled",
                "Your appointment has been cancelled for " + appointment.getTimeSlot());
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateAppointmentAccess(id, authentication);

        return appointmentRepository.findById(id)
                .map(appointment -> mapToResponse(appointment, fetchEmployeeAsync(appointment.getEmployeeId())))
                .orElseThrow(() -> new AppointmentException("Appointment not found"));
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Appointment> appointments = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(HR_ROLE))
                ? appointmentRepository.findAll()
                : appointmentRepository.findByEmployeeId(authentication.getName());

        return mapToResponses(appointments);
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByPatientId(String patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(HR_ROLE))
                && !authentication.getName().equals(patientId)) {
            throw new AppointmentException("You are not allowed to view those appointments");
        }

        return mapToResponses(appointmentRepository.findByEmployeeId(patientId));
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByMedVisitId(String medVisitId) {
        return mapToResponses(appointmentRepository.findByMedicalVisitId(Long.parseLong(medVisitId)));
    }

    private void validateTimeSlot(AppointmentRequestDTO appointmentRequestDTO, MedicalVisitResponseDTO medicalVisit) {
        LocalDateTime timeSlot = appointmentRequestDTO.timeSlot();
        LocalDateTime start = LocalDateTime.of(medicalVisit.getVisitDate(), medicalVisit.getStartTime());
        LocalDateTime end = LocalDateTime.of(medicalVisit.getVisitDate(), medicalVisit.getEndTime());

        if (timeSlot.isBefore(start) || timeSlot.isAfter(end) || timeSlot.getMinute() % 30 != 0) {
            throw new TimeSlotNotAvailableException("Time slot should be between MedicalVisit start and end time for every 30 minutes");
        }
    }

    private void validateAppointmentAccess(Long appointmentId, Authentication authentication) {
        if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(HR_ROLE))) {
            appointmentRepository.findById(appointmentId)
                    .filter(appointment -> appointment.getEmployeeId().equals(authentication.getName()))
                    .orElseThrow(() -> new AppointmentException("You are not allowed to access this appointment"));
        }
    }

    @Async
    protected CompletableFuture<EmployeeResponse> fetchEmployeeAsync(String employeeId) {
        return CompletableFuture.supplyAsync(() -> employeeClient.getEmployeeByDn(employeeId));
    }

    @Async
    protected void sendNotificationAsync(String employeeId, String subject, String message) {
        CompletableFuture.runAsync(() -> medicalVisitNotificationService.sendMailNotification(employeeId, subject, message))
                .exceptionally(throwable -> {
                    log.error("Failed to send notification for employee {}: {}", employeeId, throwable.getMessage());
                    return null;
                });
    }

    private AppointmentResponseDTO mapToResponse(Appointment appointment, CompletableFuture<EmployeeResponse> employeeFuture) {
        try {
            EmployeeResponse employee = employeeFuture.get();
            String employeeFullName = employee.firstName() + " " + employee.lastName();
            return appointmentMapper.toDto(appointment, employeeFullName, employee.email());
        } catch (Exception e) {
            log.error("Failed to fetch employee details for {}: {}", appointment.getEmployeeId(), e.getMessage());
            throw new AppointmentException("Unable to retrieve employee details");
        }
    }

    private List<AppointmentResponseDTO> mapToResponses(List<Appointment> appointments) {
        // Fetch employee details in parallel
        List<CompletableFuture<AppointmentResponseDTO>> futures = appointments.stream()
                .map(appointment -> fetchEmployeeAsync(appointment.getEmployeeId())
                        .thenApply(employee -> {
                            String employeeFullName = employee.firstName() + " " + employee.lastName();
                            return appointmentMapper.toDto(appointment, employeeFullName, employee.email());
                        }))
                .toList();

        // Collect results
        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        log.error("Error mapping appointment: {}", e.getMessage());
                        throw new AppointmentException("Failed to map appointment");
                    }
                })
                .toList();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendAsyncEventNotifications(MedicalVisitResponseDTO medVisit, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("medicalVisit", medVisit);
        CompletableFuture<Void> eventNotification = CompletableFuture.runAsync(() ->
                notificationEventService.sendEventNotification(
                        new Event(
                                eventType,
                                medVisit.getId().toString(),
                                "MEDICAL_VISIT",
                                payload
                        )
                ));

        CompletableFuture.allOf(eventNotification)
                .whenComplete((_, throwable) -> {
                    if (throwable != null) {
                        log.error("Failed to send event notifications for medical visit ID {}: {}",
                                medVisit.getId(), throwable.getMessage());
                    } else {
                        log.debug("Event notifications sent successfully for medical visit ID {}", medVisit.getId());
                    }
                });
    }


}