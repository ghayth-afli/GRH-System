package com.otbs.auth.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Data
public class User {
    private String username;
    private String authorities;
}
