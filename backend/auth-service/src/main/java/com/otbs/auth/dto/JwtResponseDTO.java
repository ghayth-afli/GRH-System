package com.otbs.auth.dto;

import com.otbs.feign.dto.EmployeeResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing JWT tokens and user details")
public record JwtResponseDTO(
        @Schema(description = "JWT access token for authentication", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String accessToken,

        @Schema(description = "JWT refresh token for obtaining new access tokens", example = "eyJhbGciOiJIUzI1NiIsIn...")
        String refreshToken,

        @Schema(description = "Expiration timestamp of the access token (in milliseconds)", example = "1697059200000")
        long accessExpiration,

        @Schema(description = "Expiration timestamp of the refresh token (in milliseconds)", example = "1697145600000")
        long refreshExpiration,

        @Schema(description = "Details of the authenticated user")
        EmployeeResponse user
) {}