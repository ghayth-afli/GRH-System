package com.otbs.recruitment.repository;

import com.otbs.recruitment.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
