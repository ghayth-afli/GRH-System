package com.otbs.attendance.repository;

import com.otbs.attendance.model.AttendanceTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AttendanceTransactionRepository extends JpaRepository<AttendanceTransaction, Integer> {

    // ... existing methods

    @Query("""
     SELECT at FROM AttendanceTransaction at
     WHERE at.employee.email = :email
     AND at.punchTime BETWEEN :startOfDay AND :endOfDay
     ORDER BY at.punchTime ASC
    """)
    List<AttendanceTransaction> findByEmployeeEmailAndPunchDate(
            @Param("email") String email,
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay
    );

    /**
     * Finds all attendance transactions for a given employee email, ordered by punch time.
     * This is useful for fetching the entire history for an employee.
     */
    @Query("""
     SELECT at FROM AttendanceTransaction at
     WHERE at.employee.email = :email
     ORDER BY at.punchTime ASC
    """)
    List<AttendanceTransaction> findAllByEmployeeEmail(@Param("email") String email);
}