package com.otbs.leave.service;

import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.model.Leave;
import com.otbs.leave.model.LeaveBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaveService {
    void applyLeave(LeaveRequest leaveRequest);
    void cancelLeave(Long leaveId);
    void approveLeave(Long leaveId);
    void rejectLeave(Long leaveId);
    Page<Leave> getLeaveHistory(String userDn,Pageable pageable);
    LeaveBalance getLeaveBalance(String userDn);
    Page<Leave> getAllLeaves(Pageable pageable);
    Page<Leave> getAllLeavesForManager(Pageable pageable);
    void updateLeave(Long leaveId, LeaveRequest leaveRequest);
    byte[] downloadAttachment(Long leaveId);
    void addMonthlyLeaveForAllEmployees();
}
