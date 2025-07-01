package com.otbs.leave.controller;

import com.otbs.leave.dto.LeaveRequestDTO;
import com.otbs.leave.dto.LeaveResponseDTO;
import com.otbs.leave.dto.MessageResponseDTO;
import com.otbs.leave.model.ELeaveType;
import com.otbs.leave.model.LeaveBalance;
import com.otbs.leave.service.LeaveService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
@Slf4j
@RestController
@Tag(name = "Leave Management", description = "APIs for managing user leave requests, approvals, and balances")
@SecurityRequirement(name = "bearerAuth")
public class LeaveController {

    private final LeaveService leaveService;

    @Operation(
            summary = "Apply for a leave",
            description = "Submits a leave request with optional attachment. Requires User role. Start and end times are required for AUTHORIZATION leave."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave applied successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., start date after end date)")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> applyLeave(
            @Parameter(description = "Type of leave", example = "ANNUAL", required = true)
            @RequestParam("leaveType") ELeaveType leaveType,
            @Parameter(description = "Start date of the leave", example = "2025-06-01", required = true)
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the leave", example = "2025-06-05", required = true)
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Start time for hourly leave (required for AUTHORIZATION)", example = "09:00:00")
            @RequestParam(value = "startHOURLY", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startHOURLY,
            @Parameter(description = "End time for hourly leave (required for AUTHORIZATION)", example = "17:00:00")
            @RequestParam(value = "endHOURLY", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endHOURLY,
            @Parameter(description = "Optional attachment (e.g., medical certificate)", required = false)
            @RequestParam(value = "attachment", required = false) MultipartFile attachment
    ) {
        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO(leaveType, startDate, endDate, startHOURLY, endHOURLY);
        leaveService.applyLeave(leaveRequestDTO, attachment);
        return ResponseEntity.ok(new MessageResponseDTO("Leave applied successfully"));
    }

    @Operation(
            summary = "Cancel a leave request",
            description = "Cancels a leave request by ID. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave cancelled successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Leave not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/cancel/{leaveId}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> cancelLeave(
            @Parameter(description = "ID of the leave request", example = "1")
            @PathVariable("leaveId") Long leaveId
    ) {
        leaveService.cancelLeave(leaveId);
        return ResponseEntity.ok(new MessageResponseDTO("Leave cancelled successfully"));
    }

    @Operation(
            summary = "Approve a leave request",
            description = "Approves a leave request by ID. Requires Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave approved successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Leave not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/approve/{leaveId}")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HRD')")
    public ResponseEntity<MessageResponseDTO> approveLeave(
            @Parameter(description = "ID of the leave request", example = "1")
            @PathVariable("leaveId") Long leaveId
    ) {
        leaveService.approveLeave(leaveId);
        return ResponseEntity.ok(new MessageResponseDTO("Leave approved successfully"));
    }

    @Operation(
            summary = "Reject a leave request",
            description = "Rejects a leave request by ID. Requires Manager role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave rejected successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "404", description = "Leave not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/reject/{leaveId}")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HRD')")
    public ResponseEntity<MessageResponseDTO> rejectLeave(
            @Parameter(description = "ID of the leave request", example = "1")
            @PathVariable("leaveId") Long leaveId
    ) {
        leaveService.rejectLeave(leaveId);
        return ResponseEntity.ok(new MessageResponseDTO("Leave rejected successfully"));
    }

    @Operation(
            summary = "Update a leave request",
            description = "Updates an existing leave request by ID. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave updated successfully",
            content = @Content(schema = @Schema(implementation = MessageResponseDTO.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Leave not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @PutMapping("/update/{leaveId}")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<MessageResponseDTO> updateLeave(
            @Parameter(description = "ID of the leave request", example = "1")
            @PathVariable("leaveId") Long leaveId,
            @Valid @RequestBody LeaveRequestDTO leaveRequestDTO,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment

    ) {
        leaveService.updateLeave(leaveId, leaveRequestDTO,attachment);
        return ResponseEntity.ok(new MessageResponseDTO("Leave updated successfully"));
    }

    @Operation(
            summary = "Get all leave requests",
            description = "Retrieves all leave requests. Requires Manager or HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of leave requests",
            content = @Content(schema = @Schema(implementation = LeaveResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<List<LeaveResponseDTO>> getAllRecievedLeaves() {
        return ResponseEntity.ok(leaveService.getAllRecievedLeavesRequests());
    }

    //get all leave requests for the authenticated user
    @Operation(
            summary = "Get all leave requests for authenticated user",
            description = "Retrieves all leave requests for the authenticated user. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of leave requests for the authenticated user",
            content = @Content(schema = @Schema(implementation = LeaveResponseDTO.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/myLeaves")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('Employee')")
    public ResponseEntity<List<LeaveResponseDTO>> getMyLeaves() {
        List<LeaveResponseDTO> myLeaves = leaveService.getAllSentLeavesRequests();
        return ResponseEntity.ok(myLeaves);
    }


    @Operation(
            summary = "Get leave balance",
            description = "Retrieves the leave balance for the authenticated user. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "User's leave balance",
            content = @Content(schema = @Schema(implementation = LeaveBalance.class))
    )
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('Employee') or hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<LeaveBalance> getLeaveBalance() {
        return ResponseEntity.ok(leaveService.getLeaveBalance());
    }


    @Operation(
            summary = "Download leave attachment",
            description = "Downloads the attachment for a leave request by ID. Requires Manager or HR role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Attachment downloaded successfully",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    )
    @ApiResponse(responseCode = "404", description = "Attachment or leave not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @GetMapping("/{leaveId}/receivedAttachment")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR') or hasAuthority('HRD')")
    public ResponseEntity<byte[]> getReceivedAttachment(
            @Parameter(description = "ID of the leave request", example = "1")
            @PathVariable("leaveId") Long leaveId
    ) {
        byte[] attachment = leaveService.downloadAttachment(leaveId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "leave-attachment.pdf");
        return new ResponseEntity<>(attachment, headers, HttpStatus.OK);
    }

    //leave exist by userDn and date
    @Operation(
            summary = "Check if leave exists for user on a specific date",
            description = "Checks if a leave request exists for the authenticated user on a specific date. Requires User role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Leave exists for the user on the specified date",
            content = @Content(schema = @Schema(implementation = Boolean.class))
    )
    @GetMapping("/exists")
    public ResponseEntity<Boolean> leaveExists(
            @Parameter(description = "Date to check for leave existence", example = "2025-06-01", required = true)
            @RequestParam("userDn") String userDn,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        boolean exists = leaveService.isUserOnLeave(userDn, date);
        return ResponseEntity.ok(exists);
    }
}