package com.otbs.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserInfoRequestDTO(

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$", message = "First name must contain only letters and spaces")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]+(?: [A-Za-z]+)*$", message = "Last name must contain only letters and spaces")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Job title is required")
        @Size(min = 2, max = 100, message = "Job title must be between 2 and 100 characters")
        String jobTitle,


        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+216\\d{8}$", message = "Phone number must be in the format +216XXXXXXXX")
        String phoneNumber1,

        @Pattern(regexp = "^\\+216\\d{8}$", message = "Phone number must be in the format +216XXXXXXXX")
        String phoneNumber2

) {}
