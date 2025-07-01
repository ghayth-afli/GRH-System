package com.otbs.leave.repository;

import com.otbs.leave.model.ELeaveType;
import com.otbs.leave.model.EStatus;
import com.otbs.leave.model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findAllByUserDn(String userDn);
    Optional<Leave> findByIdAndUserDn(Long id, String userDn);
    List<Leave> findByUserDnAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatus(
            String userDn, LocalDate date, LocalDate date2, EStatus status);
    @Query("SELECT COUNT(l) FROM Leave l WHERE " +
            "l.id <> :excludeLeaveId AND " +
            "l.department = :department AND " +
            "l.leaveType = :leaveType AND " +
            "l.status IN (:statuses) AND " +
            ":date BETWEEN l.startDate AND l.endDate")
    long countOverlappingRequests(@Param("department") String department,
                                  @Param("leaveType") ELeaveType leaveType,
                                  @Param("statuses") List<EStatus> statuses,
                                  @Param("date") LocalDate date,
                                  @Param("excludeLeaveId") Long excludeLeaveId);
}
