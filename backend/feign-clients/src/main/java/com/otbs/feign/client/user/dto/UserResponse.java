package com.otbs.feign.client.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public record UserResponse(
        @JsonDeserialize(as = String.class) String id,
        String username,
        String firstName,
        String lastName,
        String email,
        String department,
        String role,
        String jobTitle,
        String phoneNumber1,
        String phoneNumber2,
        String gender,
        String birthDate
) {
}
