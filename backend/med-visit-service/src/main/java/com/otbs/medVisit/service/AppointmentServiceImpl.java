package com.otbs.medVisit.service;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
    private final UserClient userClient;
    private final MedicalVisitMapper medicalVisitMapper;

    @Override
    @Transactional
    public void createAppointment(AppointmentRequestDTO appointment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        // Check for existing appointment
        if (appointmentRepository.existsByUserIdAndMedicalVisitId(userId, appointment.medicalVisitId())) {
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
                    .userId(userId)
                    .medicalVisit(visit)
                    .timeSlot(appointment.timeSlot())
                    .status(EAppointmentStatus.PLANIFIE)
                    .build();
            appointmentRepository.save(newAppointment);

            // Asynchronously send notification
            sendNotificationAsync(userId, "Appointment created",
                    "Your appointment has been created for " + appointment.timeSlot());
        }, () -> {
            throw new AppointmentException("Medical visit not found");
        });


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
        sendNotificationAsync(appointment.getUserId(), "Appointment updated",
                "Your appointment has been updated to " + appointmentRequestDTO.timeSlot());
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

        Appointment appointment = appointmentRepository.findByMedicalVisitIdAndUserId(id, authentication.getName())
                .orElseThrow(() -> new AppointmentException("Appointment not found"));

        appointmentRepository.deleteById(appointment.getId());
        sendNotificationAsync(appointment.getUserId(), "Appointment cancelled",
                "Your appointment has been cancelled for " + appointment.getTimeSlot());
    }

    @Override
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateAppointmentAccess(id, authentication);

        return appointmentRepository.findById(id)
                .map(appointment -> mapToResponse(appointment, fetchUserAsync(appointment.getUserId())))
                .orElseThrow(() -> new AppointmentException("Appointment not found"));
    }

    @Override
    public List<AppointmentResponseDTO> getAllAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Appointment> appointments = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(HR_ROLE))
                ? appointmentRepository.findAll()
                : appointmentRepository.findByUserId(authentication.getName());

        return mapToResponses(appointments);
    }

    @Override
    public List<AppointmentResponseDTO> getAppointmentsByPatientId(String patientId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(HR_ROLE))
                && !authentication.getName().equals(patientId)) {
            throw new AppointmentException("You are not allowed to view those appointments");
        }

        return mapToResponses(appointmentRepository.findByUserId(patientId));
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
                    .filter(appointment -> appointment.getUserId().equals(authentication.getName()))
                    .orElseThrow(() -> new AppointmentException("You are not allowed to access this appointment"));
        }
    }

    @Async
    protected CompletableFuture<UserResponse> fetchUserAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> userClient.getUserByDn(userId));
    }

    @Async
    protected void sendNotificationAsync(String userId, String subject, String message) {
        CompletableFuture.runAsync(() -> medicalVisitNotificationService.sendMailNotification(userId, subject, message))
                .exceptionally(throwable -> {
                    log.error("Failed to send notification for user {}: {}", userId, throwable.getMessage());
                    return null;
                });
    }

    private AppointmentResponseDTO mapToResponse(Appointment appointment, CompletableFuture<UserResponse> userFuture) {
        try {
            UserResponse user = userFuture.get();
            String userFullName = user.firstName() + " " + user.lastName();
            return appointmentMapper.toDto(appointment, userFullName, user.email());
        } catch (Exception e) {
            log.error("Failed to fetch user details for {}: {}", appointment.getUserId(), e.getMessage());
            throw new AppointmentException("Unable to retrieve user details");
        }
    }

    private List<AppointmentResponseDTO> mapToResponses(List<Appointment> appointments) {
        List<CompletableFuture<AppointmentResponseDTO>> futures = appointments.stream()
                .map(appointment -> fetchUserAsync(appointment.getUserId())
                        .thenApply(user -> {
                            String userFullName = user.firstName() + " " + user.lastName();
                            return appointmentMapper.toDto(appointment, userFullName, user.email());
                        }))
                .toList();

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



}