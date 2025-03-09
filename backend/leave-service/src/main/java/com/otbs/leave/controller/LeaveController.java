package com.otbs.leave.controller;

import com.otbs.feign.dto.EmployeeResponse;
import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.dto.MessageResponse;
import com.otbs.leave.model.ELeaveType;
import com.otbs.leave.model.Leave;
import com.otbs.leave.model.LeaveBalance;
import com.otbs.leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
@Slf4j
@RestController
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping(value = "/apply", consumes = { "multipart/form-data" })
    public ResponseEntity<String> applyLeave(
            @RequestParam("leaveType") ELeaveType leaveType,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            @RequestParam(value = "startHOURLY", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startHOURLY,
            @RequestParam(value = "endHOURLY", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endHOURLY) {

        LeaveRequest leaveRequest = new LeaveRequest(leaveType, startDate, endDate, attachment, startHOURLY, endHOURLY);
        leaveService.applyLeave(leaveRequest);

        return ResponseEntity.ok("Leave request submitted successfully");
    }

    @DeleteMapping("/cancel/{leaveId}")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<MessageResponse> cancelLeave(@PathVariable Long leaveId) {
        leaveService.cancelLeave(leaveId);
        return ResponseEntity.ok(new MessageResponse("Leave cancelled successfully"));
    }

    @PutMapping("/approve/{leaveId}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<MessageResponse> approveLeave(@PathVariable Long leaveId) {
        leaveService.approveLeave(leaveId);
        return ResponseEntity.ok(new MessageResponse("Leave approved successfully"));
    }

    @PutMapping("/reject/{leaveId}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<MessageResponse> rejectLeave(@PathVariable Long leaveId) {
        leaveService.rejectLeave(leaveId);
        return ResponseEntity.ok(new MessageResponse("Leave rejected successfully"));
    }

    @PutMapping("/update/{leaveId}")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<MessageResponse> updateLeave(@PathVariable Long leaveId, @RequestBody LeaveRequest leaveRequest) {
        leaveService.updateLeave(leaveId, leaveRequest);
        return ResponseEntity.ok(new MessageResponse("Leave updated successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('HR')")
    public ResponseEntity<Page<Leave>> getAllLeaves(@PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaves(pageable));
    }

    @GetMapping("/received")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<Page<Leave>> getAllReceivedLeaves(@PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeavesForManager(pageable));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<LeaveBalance> getLeaveBalance() {
        EmployeeResponse user = (EmployeeResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(leaveService.getLeaveBalance(user.id()));
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<Page<Leave>> getLeaveHistory(@PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        EmployeeResponse user = (EmployeeResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(leaveService.getLeaveHistory(user.id(), pageable));
    }

    @GetMapping("/{leaveId}/receivedAttachment")
    @PreAuthorize("hasAuthority('Manager') or hasAuthority('HR')")
    public ResponseEntity<byte[]> getReceivedAttachment(@PathVariable Long leaveId) {
        byte[] attachment = leaveService.downloadAttachment(leaveId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "leave-attachment.pdf");
        return new ResponseEntity<>(attachment, headers, HttpStatus.OK);
    }
}