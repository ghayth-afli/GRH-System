package com.otbs.leave.repository;

import com.otbs.leave.model.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findAllByUserDn(String userDn);
    Optional<Leave> findByIdAndUserDn(Long id, String userDn);
}
