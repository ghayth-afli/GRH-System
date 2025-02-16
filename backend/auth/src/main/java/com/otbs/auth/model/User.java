package com.otbs.auth.model;

import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter

public class User {
    private String username;
    private String authorities;
    private String email;
    @Column(name = "dn")
    private String dn;
}