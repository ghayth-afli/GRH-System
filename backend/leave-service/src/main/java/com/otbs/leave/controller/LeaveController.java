package com.otbs.leave.controller;

import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.dto.MessageResponse;
import com.otbs.leave.model.Leave;
import com.otbs.leave.service.LeaveService;
import com.otbs.leave.service.LeaveServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/leave")
@RequiredArgsConstructor
@Slf4j
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/apply")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<?> applyLeave(@RequestBody LeaveRequest leaveRequest) {
        leaveService.applyLeave(leaveRequest);
        return ResponseEntity.ok().body(new MessageResponse("Leave applied successfully"));
    }

    @DeleteMapping("/cancel/{leaveId}")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<?> cancelLeave(@PathVariable Long leaveId) {
        leaveService.cancelLeave(leaveId);
        return ResponseEntity.ok().body(new MessageResponse("Leave cancelled successfully"));
    }

    @PutMapping("/approve/{leaveId}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<?> approveLeave(@PathVariable Long leaveId) {
        leaveService.approveLeave(leaveId);
        return ResponseEntity.ok().body(new MessageResponse("Leave approved successfully"));
    }

    @PutMapping("/reject/{leaveId}")
    @PreAuthorize("hasAuthority('Manager')")
    public ResponseEntity<?> rejectLeave(@PathVariable Long leaveId) {
        leaveService.rejectLeave(leaveId);
        return ResponseEntity.ok().body(new MessageResponse("Leave rejected successfully"));
    }

    @PutMapping("/update/{leaveId}")
    public ResponseEntity<?> updateLeave(@PathVariable Long leaveId, @RequestBody LeaveRequest leaveRequest) {
        leaveService.updateLeave(leaveId, leaveRequest);
        return ResponseEntity.ok().body(new MessageResponse("Leave updated successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('Employee')")
    public ResponseEntity<Page<Leave>> getAllLeaves(
            @PageableDefault(size = 10, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(leaveService.getAllLeaves(pageable));
    }

}
