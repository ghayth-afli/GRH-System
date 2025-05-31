package com.otbs.medVisit.controller;

import com.otbs.medVisit.dto.AppointmentRequestDTO;
import com.otbs.medVisit.dto.AppointmentResponseDTO;
import com.otbs.medVisit.dto.MessageResponseDTO;
import com.otbs.medVisit.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Appointment Management", description = "APIs for managing medical visit appointments")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Operation(
            summary = "Get all appointments",
            description = "Retrieves a list of all medical appointments. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of appointments",
            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @Operation(
            summary = "Get appointment by ID",
            description = "Retrieves details of a specific appointment by ID. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment found",
            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<AppointmentResponseDTO> getAppointment(
            @Parameter(description = "ID of the appointment", example = "1")
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @Operation(
            summary = "Get appointments by employee ID",
            description = "Retrieves a list of appointments for a specific employee. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of appointments for the employee",
            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByEmployeeId(
            @Parameter(description = "ID of the employee", example = "emp123")
            @PathVariable("employeeId") String employeeId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(employeeId));
    }

    @Operation(
            summary = "Get appointments by medical visit ID",
            description = "Retrieves a list of appointments for a specific medical visit. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of appointments for the medical visit",
            content = @Content(schema = @Schema(implementation = AppointmentResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Medical visit not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/medVisit/{medVisitId}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByMedVisitId(
            @Parameter(description = "ID of the medical visit", example = "6")
            @PathVariable("medVisitId") String medVisitId
    ) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByMedVisitId(medVisitId));
    }

    @Operation(
            summary = "Create a new appointment",
            description = "Creates a new medical appointment with the specified details. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment created successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., past time slot)")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PostMapping
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO appointmentRequestDTO
    ) {
        appointmentService.createAppointment(appointmentRequestDTO);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment created successfully"));
    }

    @Operation(
            summary = "Update an appointment",
            description = "Updates an existing appointment with the specified details. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., past time slot)")
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> updateAppointment(
            @Parameter(description = "ID of the appointment", example = "1")
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentRequestDTO appointmentRequestDTO
    ) {
        appointmentService.updateAppointment(appointmentRequestDTO, id);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment updated successfully"));
    }

    @Operation(
            summary = "Delete an appointment",
            description = "Deletes an existing appointment by ID. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment deleted successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> deleteAppointment(
            @Parameter(description = "ID of the appointment", example = "1")
            @PathVariable("id") Long id
    ) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment deleted successfully"));
    }


    //cancel appointment by medical visit id
    @Operation(
            summary = "Cancel an appointment",
            description = "Cancels an existing appointment by ID. Requires HR, Employee, or Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Appointment cancelled successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Appointment not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('HR') or hasAuthority('Employee') or hasAuthority('Manager')")
    public ResponseEntity<MessageResponseDTO> cancelAppointment(
            @Parameter(description = "ID of the appointment", example = "1")
            @PathVariable("id") Long id
    ) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(new MessageResponseDTO("Appointment cancelled successfully"));
    }
}