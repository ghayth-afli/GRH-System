package com.otbs.user.repository;

import com.otbs.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
}
