package com.otbs.leave.mapper;

import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.model.EStatus;
import com.otbs.leave.model.Leave;
import org.springframework.stereotype.Component;

@Component
public class LeaveAttributesMapper {
    public Leave toEntity(LeaveRequest leaveRequest) {

        return Leave.builder()
                .startDate(leaveRequest.startDate())
                .endDate(leaveRequest.endDate())
                .leaveType(leaveRequest.leaveType())
                .status(EStatus.EN_ATTENTE)
                //.attachment(leaveRequest.attachment())
                .startTime(leaveRequest.startHOURLY())
                .endTime(leaveRequest.endHOURLY())
                .build();
    }

    public LeaveRequest toDto(Leave leave) {

        return new LeaveRequest(
                leave.getLeaveType(),
                leave.getStartDate(),
                leave.getEndDate(),
                //leave.getAttachment(),
                leave.getStartTime(),
                leave.getEndTime()
        );
    }

    public void updateEntity(Leave leave, LeaveRequest leaveRequest) {
        leave.setStartDate(leaveRequest.startDate());
        leave.setEndDate(leaveRequest.endDate());
        leave.setLeaveType(leaveRequest.leaveType());
        //leave.setAttachment(leaveRequest.attachment());
        leave.setStartTime(leaveRequest.startHOURLY());
        leave.setEndTime(leaveRequest.endHOURLY());
    }
}
