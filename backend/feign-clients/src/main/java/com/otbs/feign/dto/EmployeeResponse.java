package com.otbs.feign.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

public record EmployeeResponse(
        @JsonDeserialize(as = String.class) String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String department,
        String role
) {

}
