package com.otbs.feign.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeResponse {

    @JsonDeserialize(as = String.class)
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String role;
}
