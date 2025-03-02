package com.otbs.leave.service;

import com.otbs.leave.dto.LeaveRequest;
import com.otbs.leave.exception.LeaveBalanceNotFoundException;
import com.otbs.leave.exception.LeaveNotFoundException;
import com.otbs.leave.mapper.LeaveAttributesMapper;
import com.otbs.leave.model.EStatus;
import com.otbs.leave.model.Leave;
import com.otbs.leave.model.LeaveBalance;
import com.otbs.leave.repository.LeaveBalanceRepository;
import com.otbs.leave.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveBalanceRepository leaveBalanceRepository;

    private final LeaveRepository leaveRepository;

    private final LeaveAttributesMapper leaveAttributesMapper;


    @Override
    public void applyLeave(LeaveRequest leaveRequest) {

        Leave leave = leaveAttributesMapper.toEntity(leaveRequest);
        //TODO: get userDn from security context

        //TODO: check if leave balance is sufficient

        //TODO: check if leave is overlapping with existing leaves

        //TODO: check if leave is overlapping with public holidays in tunisia

        //TODO: check if leave is overlapping with weekends


        leaveRepository.saveAndFlush(leave);

        //update leave balance
        leaveBalanceRepository.findByUserDn(leave.getUserDn())
                .ifPresent(leaveBalance -> {
                    leaveBalance.setUsedLeave(leaveBalance.getUsedLeave() + (int) ChronoUnit.DAYS.between(leave.getStartDate(), leave.getEndDate()));
                    leaveBalance.setRemainingLeave(leaveBalance.getTotalLeave() - leaveBalance.getUsedLeave());
                    leaveBalanceRepository.save(leaveBalance);
                });

    }

    @Override
    public void cancelLeave(Long leaveId) {
        leaveRepository.deleteById(leaveId);

        //TODO: send email to user

        //TODO: update leave balance
        updateLeaveBalance(leaveId);
    }

    private void updateLeaveBalance(Long leaveId) {
        leaveBalanceRepository.findByUserDn(leaveRepository.findById(leaveId).get().getUserDn())
                .ifPresent(leaveBalance -> {
                    leaveBalance.setUsedLeave(leaveBalance.getUsedLeave() - (int) ChronoUnit.DAYS.between(leaveRepository.findById(leaveId).get().getStartDate(), leaveRepository.findById(leaveId).get().getEndDate()));
                    leaveBalance.setRemainingLeave(leaveBalance.getTotalLeave() - leaveBalance.getUsedLeave());
                    leaveBalanceRepository.save(leaveBalance);
                });
    }

    @Override
    public void approveLeave(Long leaveId) {

        leaveRepository.findById(leaveId)
                .ifPresent(leave -> {
                    leave.setStatus(EStatus.APPROUVÉE);
                    leaveRepository.save(leave);
                });
        //TODO: send email to user

    }

    @Override
    public void rejectLeave(Long leaveId) {

        leaveRepository.findById(leaveId)
                .ifPresent(leave -> {
                    leave.setStatus(EStatus.REFUSÉE);
                    leaveRepository.save(leave);
                });
        //TODO: send email to user

        //update leave balance
        updateLeaveBalance(leaveId);

    }

    @Override
    public List<Leave> getLeaveHistory(String userDn) {
        return leaveRepository.findAllByUserDn(userDn);
    }

    @Override
    public LeaveBalance getLeaveBalance(String userDn) {
        return leaveBalanceRepository.findByUserDn(userDn).orElseThrow(() -> new LeaveBalanceNotFoundException("Leave balance not found"));
    }

    @Override
    public String getLeaveStatus(Long leaveId) {
        return leaveRepository.findById(leaveId).map(Leave::getStatus).map(EStatus::name).orElseThrow(() -> new LeaveNotFoundException("Leave not found"));
    }

    @Override
    public Leave getLeaveDetails(Long leaveId) {
        return leaveRepository.findById(leaveId).orElseThrow(() -> new LeaveNotFoundException("Leave not found"));
    }

    @Override
    public Page<Leave> getAllLeaves(Pageable pageable) {
        return leaveRepository.findAll(pageable);
    }

    @Override
    public void updateLeave(Long leaveId, LeaveRequest leaveRequest) {
        try {
            leaveRepository.findById(leaveId)
                    .ifPresent(leave -> {
                        leaveAttributesMapper.updateEntity(leave, leaveRequest);
                        leaveRepository.save(leave);
                    });
        }
        catch (Exception e) {
            throw new LeaveNotFoundException("Leave not found");
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * *")
    public void addMonthlyLeaveForAllEmployees() {
        List<LeaveBalance> leaveBalances = leaveBalanceRepository.findAll();

        for (LeaveBalance leaveBalance : leaveBalances) {
            leaveBalance.addMonthlyLeave();
            leaveBalanceRepository.save(leaveBalance);
        }

    }
}
