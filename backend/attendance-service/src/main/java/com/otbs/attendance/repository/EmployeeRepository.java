package com.otbs.attendance.repository;
import com.otbs.attendance.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByEmpCode(String empCode);
    Optional<Employee> findByEmail(String email);
}
