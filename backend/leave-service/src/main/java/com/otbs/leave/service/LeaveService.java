package com.otbs.leave.service;

import com.otbs.leave.dto.LeaveRequestDTO;
import com.otbs.leave.dto.LeaveResponseDTO;
import com.otbs.leave.model.LeaveBalance;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LeaveService {
    void applyLeave(LeaveRequestDTO leaveRequestDTO, MultipartFile attachment) ;
    void updateLeave(Long leaveId, LeaveRequestDTO leaveRequestDTO, MultipartFile attachment);
    void cancelLeave(Long leaveId);
    void approveLeave(Long leaveId);
    void rejectLeave(Long leaveId);
    List<LeaveResponseDTO> getAllRecievedLeavesRequests();
    List<LeaveResponseDTO> getAllSentLeavesRequests();
    byte[] downloadAttachment(Long leaveId);
    LeaveBalance getLeaveBalance();
}
