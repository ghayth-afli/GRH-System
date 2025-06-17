package com.otbs.leave.mapper;

import com.otbs.leave.dto.LeaveRequestDTO;
import com.otbs.leave.dto.LeaveResponseDTO;
import com.otbs.leave.model.EStatus;
import com.otbs.leave.model.Leave;
import org.springframework.stereotype.Component;

@Component
public class LeaveAttributesMapper {
    public Leave toEntity(LeaveRequestDTO leaveRequestDTO) {

        return Leave.builder()
                .startDate(leaveRequestDTO.startDate())
                .endDate(leaveRequestDTO.endDate())
                .leaveType(leaveRequestDTO.leaveType())
                .status(EStatus.PENDING)
                .startTime(leaveRequestDTO.startHOURLY())
                .endTime(leaveRequestDTO.endHOURLY())
                .build();
    }

    public LeaveResponseDTO toDto(Leave leave) {
        return new LeaveResponseDTO(leave.getId(),leave.getUserDn().split(",")[0].split("=")[1], leave.getUserDn().split(",")[1].split("=")[1], leave.getStartDate()
                ,leave.getEndDate(), leave.getLeaveType(), leave.getStatus(), leave.getStartTime(), leave.getEndTime());
    }

    public void updateEntity(Leave leave, LeaveRequestDTO leaveRequestDTO) {
        leave.setStartDate(leaveRequestDTO.startDate());
        leave.setEndDate(leaveRequestDTO.endDate());
        leave.setLeaveType(leaveRequestDTO.leaveType());
        leave.setStartTime(leaveRequestDTO.startHOURLY());
        leave.setEndTime(leaveRequestDTO.endHOURLY());
    }
}
