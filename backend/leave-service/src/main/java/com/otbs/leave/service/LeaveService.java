package com.otbs.leave.service;

import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.model.Leave;
import com.otbs.leave.model.LeaveBalance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LeaveService {
    public void applyLeave(LeaveRequest leaveRequest);
    public void cancelLeave(Long leaveId);
    public void approveLeave(Long leaveId);
    public void rejectLeave(Long leaveId);
    public List<Leave> getLeaveHistory(String userDn);
    public LeaveBalance getLeaveBalance(String userDn);
    public String getLeaveStatus(Long leaveId);
    public Leave getLeaveDetails(Long leaveId);
    public Page<Leave> getAllLeaves(Pageable pageable);
    public void updateLeave(Long leaveId, LeaveRequest leaveRequest);
    public void addMonthlyLeaveForAllEmployees();
}
